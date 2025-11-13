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
import com.nebula.nebulaCloud.utils.DatabaseCredentialGenerator;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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

@Slf4j
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
    private final EmailService emailService;

    public ResponseEntity<List<InstanceResponse>> getAllByUserId(Long userId) {

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        List<Instance> instances = instanceRepository.findByUser(user);

        List<InstanceResponse> responses = instances.stream().map(instance -> InstanceResponse.builder()
                .id(instance.getId())
                .databaseName(instance.getDatabaseName())
                .createdAt(instance.getCreatedAt())


                .containerId(instance.getContainer().getId())
                .containerIp(instance.getContainer().getIp())
                .containerPort(instance.getContainer().getPort())
                .engineName(instance.getContainer().getEngine().getName())

                .dbUsername(instance.getUserDb().getDbUser())

                .userId(instance.getUser().getId())

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
                .createdAt(instance.getCreatedAt())


                .containerId(instance.getContainer().getId())
                .containerIp(instance.getContainer().getIp())
                .containerPort(instance.getContainer().getPort())
                .engineName(instance.getContainer().getEngine().getName())

                .dbUsername(instance.getUserDb().getDbUser())

                .userId(instance.getUser().getId())
                .build()).toList();
        return ResponseEntity.ok(responses);
    }

    public ResponseEntity<List<InstanceResponse>> getAll() {

        List<Instance> instances = instanceRepository.findAll();

        List<InstanceResponse> responses = instances.stream().map(instance -> InstanceResponse.builder()
                .id(instance.getId())
                .databaseName(instance.getDatabaseName())
                .createdAt(instance.getCreatedAt())


                .containerId(instance.getContainer().getId())
                .containerIp(instance.getContainer().getIp())
                .containerPort(instance.getContainer().getPort())
                .engineName(instance.getContainer().getEngine().getName())

                .dbUsername(instance.getUserDb().getDbUser())

                .userId(instance.getUser().getId())
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

        String dbUser;
        String dbPassword;
        String dbName;
        boolean userExistsFree = false;

        if (user.getPlan().getName().equals("FREE")){
            dbUser = DatabaseCredentialGenerator.generateRandomUser(user.getId());
            dbPassword = DatabaseCredentialGenerator.generateSecurePassword(16);
            dbName = DatabaseCredentialGenerator.generateDatabaseName(user.getId());

            Optional<Instance> existingDbNameFree = instanceRepository.findByDatabaseName(dbName);
            if (existingDbNameFree.isPresent()) {
                throw new RuntimeException("dbName ya existe");
            }

            Optional<UserDb> existingDbUserFree = userDbRepository.findByDbUser(dbUser);
            if (existingDbUserFree.isPresent()) {
                // Verificar si el dbUser pertenece al usuario actual
                if (!existingDbUserFree.get().getUser().getId().equals(user.getId())) {
                    throw new RuntimeException("El userdb ya existe");
                }
            }

            createDatabaseAndUser(engine, dbName, dbUser, dbPassword, userExistsFree);

            Instance instanceFree = Instance.builder()
                    .name(user.getUsername())
                    .databaseName(dbName)
                    .container(container)
                    .user(user)
                    .createdAt(LocalDateTime.now())
                    .build();

            UserDb userDbFree = UserDb.builder()
                    .dbUser(dbUser)
                    .dbPasswordEnc(passwordEncoder.encode(dbPassword))
                    .user(user)
                    .build();
            userDbRepository.save(userDbFree);

            instanceFree.setUserDb(userDbFree);

            instanceRepository.save(instanceFree);

            InstanceResponse response = InstanceResponse.builder()
                    .id(instanceFree.getId())
                    .databaseName(instanceFree.getDatabaseName())
                    .engineName(engine.getName())
                    .password(dbPassword)
                    .userId(instanceFree.getUser().getId())
                    .containerId(instanceFree.getContainer().getId())
                    .createdAt(instanceFree.getCreatedAt())
                    .build();

            return ResponseEntity.ok(response);
        }

        // Limpiar nombres de caracteres especiales
        String cleanDbUser = request.getDbUser().replaceAll("[^a-zA-Z0-9_]", "");
        String cleanDbName = request.getDatabaseName().replaceAll("[^a-zA-Z0-9_]", "");

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

        dbName = cleanDbName;
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

        // Send credentials email to user with the plain password (before it was encrypted)
        try {
            emailService.sendDatabaseCredentials(user.getEmail(), instance, dbPassword);
            log.info("Database credentials email sent successfully to: {}", user.getEmail());
        } catch (Exception e) {
            // Log error but don't fail the instance creation
            // The instance was created successfully, email is just a notification
            log.error("Failed to send credentials email to {}: {}", user.getEmail(), e.getMessage());
        }

        InstanceResponse response = InstanceResponse.builder()
                .id(instance.getId())
                .databaseName(instance.getDatabaseName())
                .engineName(engine.getName())
                .password(dbPassword != null ? dbPassword : dbPassword + " -ya existe") // Si es usuario existente, no mostramos la contraseña
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

            // Obtener todas las BD que ya pertenecen a este usuario (para PostgreSQL)
            List<String> userDatabases = null;
            if (engineName.equals("postgres") && userExists) {
                // Buscar el UserDb para obtener todas las instancias de este usuario
                Optional<UserDb> userDb = userDbRepository.findByDbUser(dbUser);
                if (userDb.isPresent()) {
                    // Obtener todas las instancias (BD) que pertenecen a este usuario
                    userDatabases = instanceRepository.findByUser(userDb.get().getUser())
                            .stream()
                            .map(Instance::getDatabaseName)
                            .toList();
                }
            }

            switch (engineName) {
                case "mysql" -> createMySQL(conf, dbName, dbUser, dbPassword, userExists);
                case "postgres" -> createPostgres(conf, dbName, dbUser, dbPassword, userExists, userDatabases);
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

            // Otorgar solo permisos de DML (datos) sin DDL (estructura)
            // SELECT, INSERT, UPDATE, DELETE para trabajar con datos
            // CREATE, INDEX para crear tablas e índices dentro de la BD
            // Sin DROP, ALTER, RENAME para que no pueda eliminar ni modificar la estructura de la BD
            stmt.execute("GRANT SELECT, INSERT, UPDATE, DELETE, CREATE, INDEX, SHOW VIEW, CREATE VIEW ON " + dbName + ".* TO '" + dbUser + "'@'%'");

            // Aplicar cambios
            stmt.execute("FLUSH PRIVILEGES");
        }
    }

    private void createPostgres(EngineDetails conf, String dbName, String dbUser, String dbPassword, boolean userExists, List<String> userDatabases) throws Exception {
        String url = "jdbc:postgresql://" + conf.getHost() + ":" + conf.getPort() + "/postgres";
        try (Connection conn = DriverManager.getConnection(url, conf.getRootUser(), conf.getRootPassword());
             Statement stmt = conn.createStatement()) {

            // Crear rol/usuario solo si no existe (no cambiar la contraseña si ya existe)
            if (!userExists) {
                stmt.execute("DO $$ BEGIN IF NOT EXISTS (SELECT FROM pg_roles WHERE rolname = '" + dbUser + "') THEN CREATE ROLE " + dbUser + " LOGIN PASSWORD '" + dbPassword + "'; END IF; END $$;");

                // Configurar atributos del rol (solo para usuarios nuevos)
                stmt.execute("ALTER ROLE " + dbUser + " NOCREATEDB");
                stmt.execute("ALTER ROLE " + dbUser + " NOCREATEROLE");
                stmt.execute("ALTER ROLE " + dbUser + " NOSUPERUSER");
                stmt.execute("ALTER ROLE " + dbUser + " NOREPLICATION");
                stmt.execute("ALTER ROLE " + dbUser + " NOBYPASSRLS");
                stmt.execute("ALTER ROLE " + dbUser + " NOINHERIT");
            }

            // CRÍTICO: Revocar CONNECT de postgres, template0 y template1 ANTES de crear la BD
            // Esto asegura que el usuario nunca pueda conectarse a estas BD
            stmt.execute("REVOKE CONNECT ON DATABASE postgres FROM " + dbUser);
            stmt.execute("REVOKE CONNECT ON DATABASE template0 FROM " + dbUser);
            stmt.execute("REVOKE CONNECT ON DATABASE template1 FROM " + dbUser);

            // Crear base de datos con postgres como propietario (no el usuario, para evitar que pueda eliminarla)
            stmt.execute("CREATE DATABASE " + dbName + " OWNER postgres");

            // Revocar privilegios de conexión de PUBLIC para que otros no puedan conectarse
            stmt.execute("REVOKE CONNECT ON DATABASE " + dbName + " FROM PUBLIC");

            // Otorgar conexión solo al usuario a SU base de datos (la que acabamos de crear)
            stmt.execute("GRANT CONNECT ON DATABASE " + dbName + " TO " + dbUser);

            // IMPORTANTE: Si el usuario ya tiene otras BD, otorgar CONNECT a todas ellas también
            if (userDatabases != null && !userDatabases.isEmpty()) {
                for (String existingDb : userDatabases) {
                    if (!existingDb.equals(dbName)) { // No duplicar el GRANT de la BD recién creada
                        try {
                            stmt.execute("GRANT CONNECT ON DATABASE " + existingDb + " TO " + dbUser);
                        } catch (Exception e) {
                            // Ignorar si la BD ya no existe o ya tiene el permiso
                        }
                    }
                }
            }

            // Construir lista de BD permitidas (la nueva + las existentes del usuario)
            StringBuilder allowedDatabases = new StringBuilder("'" + dbName + "'");
            if (userDatabases != null && !userDatabases.isEmpty()) {
                for (String existingDb : userDatabases) {
                    if (!existingDb.equals(dbName)) {
                        allowedDatabases.append(", '").append(existingDb).append("'");
                    }
                }
            }

            // IMPORTANTE: Revocar CONNECT de TODAS las bases de datos EXCEPTO las del usuario
            // Esto funciona tanto para usuarios nuevos como existentes
            stmt.execute("DO $$ " +
                    "DECLARE " +
                    "    db_rec RECORD; " +
                    "BEGIN " +
                    "    FOR db_rec IN SELECT datname FROM pg_database WHERE datistemplate = false AND datname NOT IN (" + allowedDatabases + ") LOOP " +
                    "        BEGIN " +
                    "            EXECUTE 'REVOKE CONNECT ON DATABASE ' || quote_ident(db_rec.datname) || ' FROM ' || quote_ident('" + dbUser + "'); " +
                    "        EXCEPTION WHEN OTHERS THEN " +
                    "            NULL; " +
                    "        END; " +
                    "    END LOOP; " +
                    "END; " +
                    "$$;");

            // CRÍTICO: Revocar permisos PUBLIC sobre pg_database en la BD postgres
            // Esto evita que los usuarios puedan ejecutar SELECT sobre pg_database
            stmt.execute("REVOKE SELECT ON pg_catalog.pg_database FROM PUBLIC");

            // CRÍTICO: Revocar permisos sobre pg_authid para evitar acceso a información de contraseñas
            stmt.execute("REVOKE ALL ON pg_catalog.pg_authid FROM PUBLIC");
            stmt.execute("REVOKE ALL ON pg_catalog.pg_authid FROM " + dbUser);

            // NOTA IMPORTANTE: PostgreSQL permite por defecto que los usuarios cambien su PROPIA contraseña
            // Esto NO se puede bloquear con Event Triggers (no soportado para ALTER ROLE)
            // La única forma de bloquearlo completamente es:
            // 1. Usar autenticación externa (LDAP, Kerberos, OAuth)
            // 2. Modificar pg_hba.conf para autenticación basada en certificados
            // 3. Usar una extensión C personalizada
            // Para este caso, se recomienda implementar rotación de contraseñas desde la aplicación
        }

        // Conectarse a la nueva base de datos para configurar permisos del schema
        String dbUrl = "jdbc:postgresql://" + conf.getHost() + ":" + conf.getPort() + "/" + dbName;
        try (Connection dbConn = DriverManager.getConnection(dbUrl, conf.getRootUser(), conf.getRootPassword());
             Statement dbStmt = dbConn.createStatement()) {

            // Revocar todos los privilegios del schema public de PUBLIC
            dbStmt.execute("REVOKE ALL ON SCHEMA public FROM PUBLIC");

            // Revocar acceso a schemas del sistema para evitar que vea catálogos
            dbStmt.execute("REVOKE ALL ON SCHEMA pg_catalog FROM " + dbUser);
            dbStmt.execute("REVOKE ALL ON SCHEMA information_schema FROM " + dbUser);

            // Otorgar solo USAGE (sin permisos adicionales) sobre pg_catalog para funcionalidad básica
            // Esto permite que funcione la conexión pero limita el acceso a tablas del catálogo
            dbStmt.execute("GRANT USAGE ON SCHEMA pg_catalog TO " + dbUser);
            dbStmt.execute("GRANT USAGE ON SCHEMA information_schema TO " + dbUser);

            // Revocar acceso específico a tablas críticas del catálogo
            // pg_database: lista de todas las bases de datos
            dbStmt.execute("REVOKE ALL ON pg_catalog.pg_database FROM PUBLIC");
            dbStmt.execute("REVOKE ALL ON pg_catalog.pg_database FROM " + dbUser);

            // pg_tablespace: información de tablespaces
            dbStmt.execute("REVOKE ALL ON pg_catalog.pg_tablespace FROM " + dbUser);

            // pg_authid: información de roles y usuarios
            dbStmt.execute("REVOKE ALL ON pg_catalog.pg_authid FROM " + dbUser);
            dbStmt.execute("REVOKE ALL ON pg_catalog.pg_roles FROM " + dbUser);

            // pg_user: información de usuarios
            dbStmt.execute("REVOKE ALL ON pg_catalog.pg_user FROM " + dbUser);
            dbStmt.execute("REVOKE ALL ON pg_catalog.pg_shadow FROM " + dbUser);

            // Configurar el search_path del usuario para que no incluya pg_catalog por defecto
            dbStmt.execute("ALTER ROLE " + dbUser + " SET search_path = public");

            // Crear una vista que solo muestre la base de datos actual
            dbStmt.execute("CREATE OR REPLACE VIEW public.my_databases AS " +
                    "SELECT current_database() AS datname, " +
                    "       current_user AS owner, " +
                    "       pg_database_size(current_database()) AS size");

            // Otorgar SELECT sobre la vista al usuario
            dbStmt.execute("GRANT SELECT ON public.my_databases TO " + dbUser);

            // Otorgar permisos específicos al usuario para trabajar con tablas
            // USAGE para usar el schema y CREATE para crear tablas/índices/secuencias
            dbStmt.execute("GRANT USAGE, CREATE ON SCHEMA public TO " + dbUser);

            // Otorgar permisos sobre todas las tablas existentes
            dbStmt.execute("GRANT SELECT, INSERT, UPDATE, DELETE ON ALL TABLES IN SCHEMA public TO " + dbUser);

            // Otorgar permisos sobre todas las secuencias existentes
            dbStmt.execute("GRANT USAGE, SELECT ON ALL SEQUENCES IN SCHEMA public TO " + dbUser);

            // Configurar privilegios por defecto para objetos futuros creados por el root
            // SELECT, INSERT, UPDATE, DELETE para tablas (sin TRUNCATE, DROP, ALTER)
            dbStmt.execute("ALTER DEFAULT PRIVILEGES FOR ROLE postgres IN SCHEMA public GRANT SELECT, INSERT, UPDATE, DELETE ON TABLES TO " + dbUser);
            dbStmt.execute("ALTER DEFAULT PRIVILEGES FOR ROLE postgres IN SCHEMA public GRANT USAGE, SELECT ON SEQUENCES TO " + dbUser);
            dbStmt.execute("ALTER DEFAULT PRIVILEGES FOR ROLE postgres IN SCHEMA public GRANT EXECUTE ON FUNCTIONS TO " + dbUser);

            // Configurar privilegios por defecto para objetos creados por el propio usuario
            dbStmt.execute("ALTER DEFAULT PRIVILEGES FOR ROLE " + dbUser + " IN SCHEMA public GRANT SELECT, INSERT, UPDATE, DELETE ON TABLES TO " + dbUser);
            dbStmt.execute("ALTER DEFAULT PRIVILEGES FOR ROLE " + dbUser + " IN SCHEMA public GRANT USAGE, SELECT ON SEQUENCES TO " + dbUser);
            dbStmt.execute("ALTER DEFAULT PRIVILEGES FOR ROLE " + dbUser + " IN SCHEMA public GRANT EXECUTE ON FUNCTIONS TO " + dbUser);
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

                // Denegar permisos a nivel de servidor para que no pueda crear/eliminar bases de datos
                stmt.execute(String.format("DENY ALTER ANY DATABASE TO [%s]", dbUser));
                stmt.execute(String.format("DENY CREATE ANY DATABASE TO [%s]", dbUser));
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

            // Asignar roles de lectura y escritura de datos (sin permisos de DDL)
            // db_datareader: puede leer todos los datos de todas las tablas
            // db_datawriter: puede insertar, actualizar y eliminar datos de todas las tablas
            // NO usar db_owner para evitar que pueda eliminar/modificar la base de datos
            dbStmt.execute(String.format(
                    "IF NOT EXISTS (SELECT 1 FROM sys.database_role_members rm " +
                    "JOIN sys.database_principals u ON rm.member_principal_id = u.principal_id " +
                    "JOIN sys.database_principals r ON rm.role_principal_id = r.principal_id " +
                    "WHERE u.name = '%s' AND r.name = 'db_datareader') " +
                    "BEGIN ALTER ROLE db_datareader ADD MEMBER [%s]; END",
                    dbUser, dbUser
            ));

            dbStmt.execute(String.format(
                    "IF NOT EXISTS (SELECT 1 FROM sys.database_role_members rm " +
                    "JOIN sys.database_principals u ON rm.member_principal_id = u.principal_id " +
                    "JOIN sys.database_principals r ON rm.role_principal_id = r.principal_id " +
                    "WHERE u.name = '%s' AND r.name = 'db_datawriter') " +
                    "BEGIN ALTER ROLE db_datawriter ADD MEMBER [%s]; END",
                    dbUser, dbUser
            ));

            // Asignar permisos para crear tablas, vistas, procedimientos, etc. pero sin poder eliminar la BD
            dbStmt.execute(String.format(
                    "IF NOT EXISTS (SELECT 1 FROM sys.database_role_members rm " +
                    "JOIN sys.database_principals u ON rm.member_principal_id = u.principal_id " +
                    "JOIN sys.database_principals r ON rm.role_principal_id = r.principal_id " +
                    "WHERE u.name = '%s' AND r.name = 'db_ddladmin') " +
                    "BEGIN ALTER ROLE db_ddladmin ADD MEMBER [%s]; END",
                    dbUser, dbUser
            ));

            // Otorgar permisos explícitos para ver esta base de datos
            dbStmt.execute(String.format("GRANT VIEW DEFINITION ON DATABASE::[%s] TO [%s]", dbName, dbUser));

            // Denegar permisos para eliminar o modificar la base de datos
            dbStmt.execute(String.format("DENY ALTER ON DATABASE::[%s] TO [%s]", dbName, dbUser));
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


}