package com.nebula.nebulaCloud.service;

import com.datastax.oss.driver.api.core.CqlSession;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoDatabase;
import com.nebula.nebulaCloud.config.EngineConfig;
import com.nebula.nebulaCloud.config.EngineDetails;
import com.nebula.nebulaCloud.dto.InstanceRequest;
import com.nebula.nebulaCloud.dto.InstanceResponse;
import com.nebula.nebulaCloud.model.*;
import com.nebula.nebulaCloud.repository.*;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import redis.clients.jedis.Jedis;

import java.net.InetSocketAddress;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.Statement;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class InstanceService {

    private final InstanceRepository instanceRepository;
    private final EngineRepository engineRepository;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final EngineConfig engineConfig;
    private final ContainerRepository containerRepository;
    private final UserDbRepository userDbRepository;

    public ResponseEntity<List<InstanceResponse>> getAllByUserId(Long userId) {

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        List<Instance> instances = instanceRepository.findByUser(user);

        List<InstanceResponse> responses = instances.stream().map(instance -> InstanceResponse.builder()
                .id(instance.getId())
                .databaseName(instance.getDatabaseName())
                .name(instance.getName())
                .engineName(instance.getContainer().getEngine().getName())
                .userId(instance.getUser().getId())
                .containerId(instance.getContainer().getId())
                .createdAt(instance.getCreatedAt())
                .build()).toList();
        return ResponseEntity.ok(responses);
    }

    public ResponseEntity<List<InstanceResponse>> getAllByEngineId(Long userId, Long engineId) {

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        Engine engine = engineRepository.findById(engineId)
                .orElseThrow(() -> new RuntimeException("Engine not found"));

        Container container = containerRepository.findByEngine(engine)
            .orElseThrow(() -> new RuntimeException("Container not found"));

        List<Instance> instances = instanceRepository.findByContainerAndUser(container, user);

        List<InstanceResponse> responses = instances.stream().map(instance -> InstanceResponse.builder()
                .id(instance.getId())
                .databaseName(instance.getDatabaseName())
                .name(instance.getName())
                .engineName(instance.getContainer().getEngine().getName())
                .userId(instance.getUser().getId())
                .containerId(instance.getContainer().getId())
                .createdAt(instance.getCreatedAt())
                .build()).toList();
        return ResponseEntity.ok(responses);
    }

    public ResponseEntity<List<InstanceResponse>> getAll() {

        List<Instance> instances = instanceRepository.findAll();

        List<InstanceResponse> responses = instances.stream().map(instance -> InstanceResponse.builder()
                .id(instance.getId())
                .databaseName(instance.getDatabaseName())
                .name(instance.getName())
                .engineName(instance.getContainer().getEngine().getName())
                .userId(instance.getUser().getId())
                .containerId(instance.getContainer().getId())
                .createdAt(instance.getCreatedAt())
                .build()).toList();
        return ResponseEntity.ok(responses);
    }

    @Transactional
    public ResponseEntity<InstanceResponse> create(InstanceRequest request) {

        User user = userRepository.findById(request.getUser())
                .orElseThrow(() -> new RuntimeException("User not found"));

        int userInstancesCount = instanceRepository.findByUser(user).size();
        if (userInstancesCount >= user.getPlan().getMaxInstances()) {
            throw new RuntimeException("Instance limit reached for user plan");
        }

        Engine engine = engineRepository.findById(request.getEngineId())
                .orElseThrow(() -> new RuntimeException("Engine not found"));

        Container container = containerRepository.findByEngine(engine)
                .orElseThrow(() -> new RuntimeException("No container available for engine: " + engine.getName()));

        // Limpiar nombres de caracteres especiales
        String cleanDbUser = request.getDbUser().replaceAll("\\W+", "");
        String cleanDbName = request.getDatabaseName().replaceAll("\\W+", "");

        // Validar si el dbName ya existe
        Optional<Instance> existingDbName = instanceRepository.findByDatabaseName(cleanDbName);
        if (existingDbName.isPresent()) {
            throw new RuntimeException("dbName ya existe");
        }

        // Validar si el dbUser ya existe
        Optional<UserDb> existingDbUser = userDbRepository.findByDbUser(cleanDbUser);
        if (existingDbUser.isPresent()) {
            // Verificar si el dbUser pertenece al usuario actual
            if (!existingDbUser.get().getUser().getId().equals(user.getId())) {
                throw new RuntimeException("El userdb ya existe");
            }
        }

        String dbUser;
        String dbPassword;
        String dbName = cleanDbName;
        boolean userExists = false;

        if (existingDbUser.isPresent() && existingDbUser.get().getUser().getId().equals(user.getId())) {
            // Reutilizar credenciales existentes del usuario
            userExists = true;
            dbUser = existingDbUser.get().getDbUser();
            dbPassword = null; // No necesitamos la contraseña en texto plano
        } else {
            // Crear nuevas credenciales
            dbUser = cleanDbUser;
            dbPassword = "pass_" + user.getId() + UUID.randomUUID().toString().substring(0, 8);
        }

        createDatabaseAndUser(engine, dbName, dbUser, dbPassword, userExists);

        Instance instance = Instance.builder()
                .name(user.getUsername())
                .databaseName(dbName)
                .container(container)
                .user(user)
                .createdAt(LocalDateTime.now())
                .build();

        UserDb userDb;
        if (userExists) {
            // Reutilizar el UserDb existente
            userDb = existingDbUser.get();
        } else {
            // Crear nuevo UserDb
            userDb = UserDb.builder()
                    .dbUser(dbUser)
                    .dbPasswordEnc(passwordEncoder.encode(dbPassword))
                    .user(user)
                    .build();
            userDbRepository.save(userDb);
        }

        instance.setUserDb(userDb);

        instanceRepository.save(instance);

        InstanceResponse response = InstanceResponse.builder()
                .id(instance.getId())
                .databaseName(instance.getDatabaseName())
                .name(instance.getName())
                .engineName(engine.getName())
                .password(dbPassword != null ? dbPassword : "********") // Si es usuario existente, no mostramos la contraseña
                .userId(instance.getUser().getId())
                .containerId(instance.getContainer().getId())
                .createdAt(instance.getCreatedAt())
                .build();

        return ResponseEntity.ok(response);
    }

    private void createDatabaseAndUser(Engine engine, String dbName, String dbUser, String dbPassword, boolean userExists) {
        try {
            String engineName = engine.getName().toLowerCase();
            EngineDetails conf = getEngineConfig(engineName);

            switch (engineName) {
                case "mysql" -> createMySQL(conf, dbName, dbUser, dbPassword, userExists);
                case "postgres" -> createPostgres(conf, dbName, dbUser, dbPassword, userExists);
                case "sqlserver" -> createSQLServer(conf, dbName, dbUser, dbPassword, userExists);
                case "mongodb" -> createMongoDB(conf, dbName, dbUser, dbPassword);
                case "cassandra" -> createCassandra(conf, dbName, dbUser, dbPassword);
                case "redis" -> createRedis(conf, dbUser, dbPassword);
                default -> throw new UnsupportedOperationException("Engine not supported: " + engineName);
            }

        } catch (Exception e) {
            throw new RuntimeException("Error creating DB/user in " + engine.getName() + ": " + e.getMessage(), e);
        }
    }

    private EngineDetails getEngineConfig(String name) {
        return switch (name.toLowerCase()) {
            case "mysql" -> engineConfig.getMysql();
            case "postgres" -> engineConfig.getPostgres();
            case "sqlserver" -> engineConfig.getSqlserver();
            case "mongodb" -> engineConfig.getMongodb();
            case "cassandra" -> engineConfig.getCassandra();
            case "redis" -> engineConfig.getRedis();
            default -> throw new IllegalArgumentException("Unknown engine: " + name);
        };
    }



    private void createMySQL(EngineDetails conf, String dbName, String dbUser, String dbPassword, boolean userExists) throws Exception {
        String url = "jdbc:mysql://" + conf.getHost() + ":" + conf.getPort() + "/?allowPublicKeyRetrieval=true&useSSL=false";
        try (Connection conn = DriverManager.getConnection(url, conf.getRootUser(), conf.getRootPassword());
             Statement stmt = conn.createStatement()) {

            // Crear base de datos si no existe
            stmt.execute("CREATE DATABASE IF NOT EXISTS " + dbName);

            // Crear usuario solo si no existe (no cambiar la contraseña si ya existe)
            if (!userExists) {
                stmt.execute("CREATE USER IF NOT EXISTS '" + dbUser + "'@'%' IDENTIFIED BY '" + dbPassword + "'");
            }

            // Otorgar todos los permisos sobre esta base de datos específica
            // El usuario solo tendrá permisos sobre las bases de datos que se le otorguen explícitamente
            stmt.execute("GRANT ALL PRIVILEGES ON " + dbName + ".* TO '" + dbUser + "'@'%'");

            // Aplicar cambios
            stmt.execute("FLUSH PRIVILEGES");
        }
    }

    private void createPostgres(EngineDetails conf, String dbName, String dbUser, String dbPassword, boolean userExists) throws Exception {
        String url = "jdbc:postgresql://" + conf.getHost() + ":" + conf.getPort() + "/postgres";
        try (Connection conn = DriverManager.getConnection(url, conf.getRootUser(), conf.getRootPassword());
             Statement stmt = conn.createStatement()) {

            // Crear rol/usuario solo si no existe (no cambiar la contraseña si ya existe)
            if (!userExists) {
                stmt.execute("DO $$ BEGIN IF NOT EXISTS (SELECT FROM pg_roles WHERE rolname = '" + dbUser + "') THEN CREATE ROLE " + dbUser + " LOGIN PASSWORD '" + dbPassword + "'; END IF; END $$;");
            }

            // Crear base de datos con el usuario como propietario
            stmt.execute("CREATE DATABASE " + dbName + " OWNER " + dbUser);

            // Revocar privilegios de conexión de PUBLIC para que otros no puedan conectarse
            stmt.execute("REVOKE CONNECT ON DATABASE " + dbName + " FROM PUBLIC");

            // Otorgar conexión solo al usuario propietario
            stmt.execute("GRANT CONNECT ON DATABASE " + dbName + " TO " + dbUser);
        }

        // Conectarse a la nueva base de datos para configurar permisos del schema
        String dbUrl = "jdbc:postgresql://" + conf.getHost() + ":" + conf.getPort() + "/" + dbName;
        try (Connection dbConn = DriverManager.getConnection(dbUrl, conf.getRootUser(), conf.getRootPassword());
             Statement dbStmt = dbConn.createStatement()) {

            // Revocar todos los privilegios del schema public de PUBLIC
            dbStmt.execute("REVOKE ALL ON SCHEMA public FROM PUBLIC");

            // Otorgar todos los privilegios del schema public solo al usuario
            dbStmt.execute("GRANT ALL ON SCHEMA public TO " + dbUser);

            // Otorgar privilegios de uso y creación en el schema
            dbStmt.execute("GRANT USAGE, CREATE ON SCHEMA public TO " + dbUser);

            // Configurar privilegios por defecto para objetos futuros
            dbStmt.execute("ALTER DEFAULT PRIVILEGES IN SCHEMA public GRANT ALL ON TABLES TO " + dbUser);
            dbStmt.execute("ALTER DEFAULT PRIVILEGES IN SCHEMA public GRANT ALL ON SEQUENCES TO " + dbUser);
            dbStmt.execute("ALTER DEFAULT PRIVILEGES IN SCHEMA public GRANT ALL ON FUNCTIONS TO " + dbUser);
        }
    }

    private void createSQLServer(EngineDetails conf, String dbName, String dbUser, String dbPassword, boolean userExists) throws Exception {
        String url = "jdbc:sqlserver://" + conf.getHost() + ":" + conf.getPort() +
                ";encrypt=true;trustServerCertificate=true";
        try (Connection conn = DriverManager.getConnection(url, conf.getRootUser(), conf.getRootPassword());
             Statement stmt = conn.createStatement()) {

            System.out.printf(url);
            // Crear login solo si no existe y si el usuario no existe previamente
            if (!userExists) {
                stmt.execute(String.format(
                        "IF NOT EXISTS (SELECT name FROM sys.server_principals WHERE name = '%s') " +
                        "BEGIN CREATE LOGIN [%s] WITH PASSWORD = '%s'; END",
                        dbUser, dbUser, dbPassword
                ));
            }

            // Crear base de datos si no existe
            stmt.execute(String.format(
                    "IF NOT EXISTS (SELECT name FROM sys.databases WHERE name = '%s') " +
                    "BEGIN CREATE DATABASE [%s]; END",
                    dbName, dbName
            ));

            // Denegar ver otras bases de datos al usuario (solo ve las suyas)
            stmt.execute(String.format("DENY VIEW ANY DATABASE TO [%s]", dbUser));

        }

        // Conectarse a la base de datos específica para crear el usuario dentro de ella
        String dbUrl = "jdbc:sqlserver://" + conf.getHost() + ":" + conf.getPort() +
                ";databaseName=" + dbName + ";encrypt=true;trustServerCertificate=true";
        try (Connection dbConn = DriverManager.getConnection(dbUrl, conf.getRootUser(), conf.getRootPassword());
             Statement dbStmt = dbConn.createStatement()) {

            // Crear usuario dentro de la base de datos si no existe
            dbStmt.execute(String.format(
                    "IF NOT EXISTS (SELECT name FROM sys.database_principals WHERE name = '%s') " +
                    "BEGIN CREATE USER [%s] FOR LOGIN [%s]; END",
                    dbUser, dbUser, dbUser
            ));

            // Asignar rol de db_owner al usuario en esta base de datos
            dbStmt.execute(String.format(
                    "IF NOT EXISTS (SELECT 1 FROM sys.database_role_members rm " +
                    "JOIN sys.database_principals u ON rm.member_principal_id = u.principal_id " +
                    "JOIN sys.database_principals r ON rm.role_principal_id = r.principal_id " +
                    "WHERE u.name = '%s' AND r.name = 'db_owner') " +
                    "BEGIN ALTER ROLE db_owner ADD MEMBER [%s]; END",
                    dbUser, dbUser
            ));

            // Otorgar permisos explícitos para ver esta base de datos
            dbStmt.execute(String.format("GRANT VIEW DEFINITION ON DATABASE::[%s] TO [%s]", dbName, dbUser));
        }
    }

    private void createMongoDB(EngineDetails conf, String dbName, String dbUser, String dbPassword) {
        String uri = "mongodb://" + conf.getRootUser() + ":" + conf.getRootPassword() + "@" + conf.getHost() + ":" + conf.getPort() + "/admin";
        try (MongoClient mongoClient = MongoClients.create(uri)) {
            MongoDatabase db = mongoClient.getDatabase(dbName);
            db.runCommand(new org.bson.Document("createUser", dbUser)
                    .append("pwd", dbPassword)
                    .append("roles", java.util.List.of(new org.bson.Document("role", "readWrite").append("db", dbName))));
        }
    }

    private void createCassandra(EngineDetails conf, String keyspace, String dbUser, String dbPassword) {
        try (CqlSession session = CqlSession.builder()
                .addContactPoint(new InetSocketAddress(conf.getHost(), conf.getPort()))
                .withAuthCredentials(conf.getRootUser(), conf.getRootPassword())
                .withLocalDatacenter("datacenter1")
                .build()) {

            session.execute("CREATE ROLE IF NOT EXISTS " + dbUser + " WITH PASSWORD = '" + dbPassword + "' AND LOGIN = true");
            session.execute("CREATE KEYSPACE IF NOT EXISTS " + keyspace + " WITH replication = {'class':'SimpleStrategy','replication_factor':1}");
            session.execute("GRANT ALL PERMISSIONS ON KEYSPACE " + keyspace + " TO " + dbUser);
        }
    }

    private void createRedis(EngineDetails conf, String dbUser, String dbPassword) {
        try (Jedis jedis = new Jedis(conf.getHost(), conf.getPort())) {
            jedis.auth(conf.getRootPassword());
            jedis.aclSetUser("user_" + dbUser, "on", ">" + dbPassword, "~*", "+@all");
        }
    }

    /*
    private EngineConfig.EngineProperties getEngineConfig(String name) {
        return switch (name) {
            case "mysql" -> engineConfig.getMysql();
            case "postgres" -> engineConfig.getPostgres();
            case "sqlserver" -> engineConfig.getSqlserver();
            case "mongodb" -> engineConfig.getMongodb();
            case "cassandra" -> engineConfig.getCassandra();
            case "redis" -> engineConfig.getRedis();
            default -> throw new IllegalArgumentException("Unknown engine: " + name);
        };
    }*/
}