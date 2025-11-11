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

    @Transactional
    public ResponseEntity<InstanceResponse> create(InstanceRequest request) {

        User user = userRepository.findById(request.getUser())
                .orElseThrow(() -> new RuntimeException("User not found"));

        Engine engine = engineRepository.findById(request.getEngineId())
                .orElseThrow(() -> new RuntimeException("Engine not found"));

        Container container = containerRepository.findByEngine(engine)
                .orElseThrow(() -> new RuntimeException("No container available for engine: " + engine.getName()));

        String dbUser = "user_" + user.getId() + "_" + request.getDbUser();
        String dbPassword = "pass_" + user.getId() +(int) (Math.random() * 10000);
        String dbName = "db_" + user.getId() + "_" + request.getDatabaseName();

        createDatabaseAndUser(engine, dbName, dbUser, dbPassword);

        Instance instance = Instance.builder()
                .name(user.getUsername())
                .databaseName(dbName)
                .container(container)
                .user(user)
                .createdAt(LocalDateTime.now())
                .build();

        UserDb userDb = UserDb.builder()
                .dbUser(dbUser)
                .dbPasswordEnc(passwordEncoder.encode(dbPassword))
                .user(user)
                .build();

        userDbRepository.save(userDb);

        instance.setUserDb(userDb);

        instanceRepository.save(instance);

        InstanceResponse response = InstanceResponse.builder()
                .id(instance.getId())
                .databaseName(instance.getDatabaseName())
                .name(instance.getName())
                .engineName(engine.getName())
                .password(dbPassword)
                .userId(instance.getUser().getId())
                .containerId(instance.getContainer().getId())
                .createdAt(instance.getCreatedAt())
                .build();

        return ResponseEntity.ok(response);
    }

    private void createDatabaseAndUser(Engine engine, String dbName, String dbUser, String dbPassword) {
        try {
            String engineName = engine.getName().toLowerCase();
            EngineDetails conf = getEngineConfig(engineName);

            switch (engineName) {
                case "mysql" -> createMySQL(conf, dbName, dbUser, dbPassword);
                case "postgres" -> createPostgres(conf, dbName, dbUser, dbPassword);
                case "sqlserver" -> createSQLServer(conf, dbName, dbUser, dbPassword);
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



    private void createMySQL(EngineDetails conf, String dbName, String dbUser, String dbPassword) throws Exception {
        String url = "jdbc:mysql://" + conf.getHost() + ":" + conf.getPort() + "/?allowPublicKeyRetrieval=true&useSSL=false";
        try (Connection conn = DriverManager.getConnection(url, conf.getRootUser(), conf.getRootPassword());
             Statement stmt = conn.createStatement()) {
            stmt.execute("CREATE DATABASE IF NOT EXISTS " + dbName);
            stmt.execute("CREATE USER IF NOT EXISTS '" + dbUser + "'@'%' IDENTIFIED BY '" + dbPassword + "'");
            stmt.execute("GRANT ALL PRIVILEGES ON " + dbName + ".* TO '" + dbUser + "'@'%'");
        }
    }

    private void createPostgres(EngineDetails conf, String dbName, String dbUser, String dbPassword) throws Exception {
        String url = "jdbc:postgresql://" + conf.getHost() + ":" + conf.getPort() + "/postgres";
        try (Connection conn = DriverManager.getConnection(url, conf.getRootUser(), conf.getRootPassword());
             Statement stmt = conn.createStatement()) {
            stmt.execute("DO $$ BEGIN IF NOT EXISTS (SELECT FROM pg_roles WHERE rolname = '" + dbUser + "') THEN CREATE ROLE " + dbUser + " LOGIN PASSWORD '" + dbPassword + "'; END IF; END $$;");
            stmt.execute("CREATE DATABASE " + dbName + " OWNER " + dbUser);
        }
    }

    private void createSQLServer(EngineDetails conf, String dbName, String dbUser, String dbPassword) throws Exception {
        String url = "jdbc:sqlserver://" + conf.getHost() + ":" + conf.getPort() +
                ";encrypt=true;trustServerCertificate=true";
        try (Connection conn = DriverManager.getConnection(url, conf.getRootUser(), conf.getRootPassword());
             Statement stmt = conn.createStatement()) {

            // Crear login si no existe (solo una vez por usuario)
            stmt.execute(String.format(
                    "IF NOT EXISTS (SELECT name FROM sys.server_principals WHERE name = '%s') " +
                    "BEGIN CREATE LOGIN [%s] WITH PASSWORD = '%s'; END",
                    dbUser, dbUser, dbPassword
            ));

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