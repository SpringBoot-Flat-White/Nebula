package com.nebula.nebulaCloud.service;

import com.datastax.oss.driver.api.core.CqlSession;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoDatabase;
import com.nebula.nebulaCloud.config.EngineDetails;
import com.nebula.nebulaCloud.config.EngineConfig;
import com.nebula.nebulaCloud.dto.*;
import com.nebula.nebulaCloud.exception.*;
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
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Service for managing database instances.
 */
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

    // TODO: Get all instances for a specific user
    public ResponseEntity<List<InstanceResponse>> getAllByUserId(Long userId) {

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with ID: " + userId));

        List<Instance> instances = instanceRepository.findByUser(user);

        List<InstanceResponse> responses = instances.stream().map(instance -> InstanceResponse.builder()
                .id(instance.getId())
                .databaseName(instance.getDatabaseName())
                .createdAt(instance.getCreatedAt())


                .containerId(instance.getContainer().getId())
                .containerIp(instance.getContainer().getIp())
                .containerPort(instance.getContainer().getPort())
                .engineName(instance.getContainer().getEngine().getName())
                .status(instance.getStatus().name())

                .dbUsername(instance.getUserDb().getDbUser())

                .userId(instance.getUser().getId())

                .build()).toList();
        return ResponseEntity.ok(responses);
    }

    public ResponseEntity<List<InstanceResponse>> getAllByEngineId(Long userId, Long engineId) {

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with ID: " + userId));

        Engine engine = engineRepository.findById(engineId)
                .orElseThrow(() -> new ResourceNotFoundException("Engine not found with ID: " + engineId));

        Container container = containerRepository.findByEngine(engine)
            .orElseThrow(() -> new ResourceNotFoundException("Container not found for engine: " + engine.getName()));

        List<Instance> instances = instanceRepository.findByContainerAndUser(container, user);

        List<InstanceResponse> responses = instances.stream().map(instance -> InstanceResponse.builder()
                .id(instance.getId())
                .databaseName(instance.getDatabaseName())
                .createdAt(instance.getCreatedAt())

                .containerId(instance.getContainer().getId())
                .containerIp(instance.getContainer().getIp())
                .containerPort(instance.getContainer().getPort())
                .engineName(instance.getContainer().getEngine().getName())
                .status(instance.getStatus().name())

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
    public ResponseEntity<InstanceResponse> updateInstance( InstanceUpdateRequest request) {
        // Validar que el usuario existe
        User user = userRepository.findById(request.getUser())
                .orElseThrow(() -> new ResourceNotFoundException("User not found with ID: " + request.getUser()));

        // Validar que la instancia existe
        Instance instance = instanceRepository.findById(request.getInstanceId())
                .orElseThrow(() -> new ResourceNotFoundException("Instance not found with ID: " + request.getInstanceId()));

        // Validar que la instancia pertenece al usuario
        if (!instance.getUser().getId().equals(user.getId())) {
            throw new UnauthorizedAccessException("Instance does not belong to this user");
        }

        // Validar que el estado es válido (solo SUSPENDED o RUNNING)
        if (request.getStatus() != Instance.Status.SUSPENDED && request.getStatus() != Instance.Status.RUNNING) {
            throw new InvalidStatusTransitionException("Invalid status. Only SUSPENDED or RUNNING are allowed");
        }

        // Si el estado no cambia, no hacer nada
        if (instance.getStatus() == request.getStatus()) {
            throw new InvalidStatusTransitionException("Instance is already in " + request.getStatus() + " status");
        }

        // Obtener información del motor y contenedor
        Engine engine = instance.getContainer().getEngine();
        String dbUser = instance.getUserDb().getDbUser();
        String dbName = instance.getDatabaseName();

        // Actualizar permisos en el motor de base de datos
        updateDatabaseAccess(engine, dbName, dbUser, instance.getStatus().equals(Instance.Status.SUSPENDED)? Instance.Status.RUNNING : Instance.Status.SUSPENDED);

        // Actualizar el estado de la instancia
        instance.setStatus(instance.getStatus().equals(Instance.Status.SUSPENDED)? Instance.Status.RUNNING : Instance.Status.SUSPENDED);
        instance.setUpdatedAt(LocalDateTime.now());
        instanceRepository.save(instance);

        // Construir respuesta
        InstanceResponse response = InstanceResponse.builder()
                .id(instance.getId())
                .databaseName(instance.getDatabaseName())
                .engineName(engine.getName())
                .userId(instance.getUser().getId())
                .containerId(instance.getContainer().getId())
                .containerIp(instance.getContainer().getIp())
                .containerPort(instance.getContainer().getPort())
                .dbUsername(dbUser)
                .createdAt(instance.getCreatedAt())
                .build();

        return ResponseEntity.ok(response);
    }

    @Transactional
    public ResponseEntity<InstanceResponse> create(InstanceRequest request) {

        User user = userRepository.findById(request.getUser())
                .orElseThrow(() -> new ResourceNotFoundException("User not found with ID: " + request.getUser()));

        int userInstancesCount = instanceRepository.findByUser(user).size();
        if (userInstancesCount >= user.getPlan().getMaxInstances()) {
            throw new InstanceLimitExceededException("Instance limit reached for user plan. Maximum allowed: " + user.getPlan().getMaxInstances());
        }

        Engine engine = engineRepository.findById(request.getEngineId())
                .orElseThrow(() -> new ResourceNotFoundException("Engine not found with ID: " + request.getEngineId()));

        Container container = containerRepository.findByEngine(engine)
                .orElseThrow(() -> new ResourceNotFoundException("No container available for engine: " + engine.getName()));

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
                throw new DuplicateResourceException("Database name already exists: " + dbName);
            }

            Optional<UserDb> existingDbUserFree = userDbRepository.findByDbUser(dbUser);
            if (existingDbUserFree.isPresent()) {
                // Verificar si el dbUser pertenece al usuario actual
                if (!existingDbUserFree.get().getUser().getId().equals(user.getId())) {
                    throw new DuplicateResourceException("Database user already exists: " + dbUser);
                }
            }

            createDatabaseAndUser(engine, dbName, dbUser, dbPassword, userExistsFree);

            Instance instanceFree = Instance.builder()
                    .name(user.getUsername())
                    .databaseName(dbName)
                    .container(container)
                    .user(user)
                    .status(Instance.Status.RUNNING)
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
                    .dbUsername(dbUser)
                    .userId(instanceFree.getUser().getId())
                    .containerId(instanceFree.getContainer().getId())
                    .containerIp(instanceFree.getContainer().getIp())
                    .containerPort(instanceFree.getContainer().getPort())
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
            throw new DuplicateResourceException("Database name already exists: " + cleanDbName);
        }

        // Validar si el dbUser ya existe
        Optional<UserDb> existingDbUser = userDbRepository.findByDbUser(cleanDbUser);
        if (existingDbUser.isPresent()) {

            if (!existingDbUser.get().getUser().getId().equals(user.getId())) {
                throw new DuplicateResourceException("Database user already exists: " + cleanDbUser);
            }
        }

        dbName = cleanDbName;
        boolean userExists = false;

        if (existingDbUser.isPresent() && existingDbUser.get().getUser().getId().equals(user.getId())) {

            userExists = true;
            dbUser = existingDbUser.get().getDbUser();
            dbPassword = null;
        } else {

            dbUser = cleanDbUser;

            dbPassword = (request.getDbPassword() != null && !request.getDbPassword().trim().isEmpty())
                    ? request.getDbPassword()
                    : "pass_" + user.getId() + UUID.randomUUID().toString().substring(0, 8);
        }

        createDatabaseAndUser(engine, dbName, dbUser, dbPassword, userExists);

        Instance instance = Instance.builder()
                .name(user.getUsername())
                .databaseName(dbName)
                .container(container)
                .user(user)
                .status(Instance.Status.RUNNING)
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
                .dbUsername(instance.getUserDb().getDbUser())
                .containerIp(instance.getContainer().getIp())
                .status(instance.getStatus().name())
                .containerPort(instance.getContainer().getPort())
                .password(dbPassword != null ? dbPassword : "Usuario existente - usando credenciales anteriores") // Si es usuario existente, no mostramos la contraseña
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

            // Obtener todas las BD que ya pertenecen a este usuario (para motores que lo necesiten)
            List<String> userDatabases = null;
            if (userExists) {
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
                case "mysql" -> createMySQL(conf, dbName, dbUser, dbPassword, userExists, userDatabases);
                case "postgres" -> createPostgres(conf, dbName, dbUser, dbPassword, userExists, userDatabases);
                case "sqlserver" -> createSQLServer(conf, dbName, dbUser, dbPassword, userExists, userDatabases);
                case "mongodb" -> createMongoDB(conf, dbName, dbUser, dbPassword);
                case "cassandra" -> createCassandra(conf, dbName, dbUser, dbPassword);
                case "redis" -> createRedis(conf, dbUser, dbPassword);
                default -> throw new UnsupportedOperationException("Engine not supported: " + engineName);
            }

        } catch (Exception e) {
            log.error("Error creating database/user in {}: {}", engine.getName(), e.getMessage(), e);
            throw new DatabaseOperationException("Error creating database/user in " + engine.getName() + ": " + e.getMessage(), e);
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



    private void createMySQL(EngineDetails conf, String dbName, String dbUser, String dbPassword, boolean userExists, List<String> userDatabases) throws Exception {
        String url = "jdbc:mysql://" + conf.getHost() + ":" + conf.getPort() + "/?allowPublicKeyRetrieval=true&useSSL=false";
        try (Connection conn = DriverManager.getConnection(url, conf.getRootUser(), conf.getRootPassword());
             Statement stmt = conn.createStatement()) {

            // Crear base de datos si no existe
            stmt.execute("CREATE DATABASE IF NOT EXISTS " + dbName);

            // Crear usuario solo si no existe (no cambiar la contraseña si ya existe)
            if (!userExists) {
                // Crear usuario sin ningún privilegio global
                stmt.execute("CREATE USER IF NOT EXISTS '" + dbUser + "'@'%' IDENTIFIED BY '" + dbPassword + "'");

                // Bloquear que el usuario pueda cambiar su propia contraseña
                // En MySQL, para evitar ALTER USER CURRENT_USER(), necesitamos asegurarnos
                // que el usuario NO tenga privilegios globales que le permitan esto
                // Primero revocamos TODOS los privilegios globales si existieran
                stmt.execute("REVOKE ALL PRIVILEGES, GRANT OPTION FROM '" + dbUser + "'@'%'");
            }

            // Otorgar permisos sobre la nueva base de datos
            // SELECT, INSERT, UPDATE, DELETE para trabajar con datos
            // CREATE, INDEX para crear tablas e índices dentro de la BD
            // Sin DROP, ALTER, RENAME para que no pueda eliminar ni modificar la estructura de la BD
            stmt.execute("GRANT SELECT, INSERT, UPDATE, DELETE, CREATE, INDEX, SHOW VIEW, CREATE VIEW ON " + dbName + ".* TO '" + dbUser + "'@'%'");

            // Si el usuario ya existe, asegurar que tenga acceso a todas sus bases de datos anteriores
            if (userExists && userDatabases != null && !userDatabases.isEmpty()) {
                for (String existingDb : userDatabases) {
                    if (!existingDb.equals(dbName)) { // No duplicar el GRANT de la BD recién creada
                        try {
                            // Re-otorgar permisos sobre bases de datos existentes (por si fueron revocados)
                            stmt.execute("GRANT SELECT, INSERT, UPDATE, DELETE, CREATE, INDEX, SHOW VIEW, CREATE VIEW ON " + existingDb + ".* TO '" + dbUser + "'@'%'");
                        } catch (Exception e) {
                            // Ignorar si la BD ya no existe o ya tiene el permiso
                            log.warn("Could not grant permissions on existing database {}: {}", existingDb, e.getMessage());
                        }
                    }
                }
            }

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

    private void createSQLServer(EngineDetails conf, String dbName, String dbUser, String dbPassword, boolean userExists, List<String> userDatabases) throws Exception {
        String url = "jdbc:sqlserver://" + conf.getHost() + ":" + conf.getPort() +
                ";encrypt=true;trustServerCertificate=true";
        try (Connection conn = DriverManager.getConnection(url, conf.getRootUser(), conf.getRootPassword());
             Statement stmt = conn.createStatement()) {

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

        // Si el usuario ya existe, asegurar que tenga acceso a todas sus bases de datos anteriores
        if (userExists && userDatabases != null && !userDatabases.isEmpty()) {
            for (String existingDb : userDatabases) {
                if (!existingDb.equals(dbName)) { // No duplicar en la BD recién creada
                    try {
                        String existingDbUrl = "jdbc:sqlserver://" + conf.getHost() + ":" + conf.getPort() +
                                ";databaseName=" + existingDb + ";encrypt=true;trustServerCertificate=true";
                        try (Connection existingDbConn = DriverManager.getConnection(existingDbUrl, conf.getRootUser(), conf.getRootPassword());
                             Statement existingDbStmt = existingDbConn.createStatement()) {

                            // Crear usuario dentro de la base de datos existente si no existe
                            existingDbStmt.execute(String.format(
                                    "IF NOT EXISTS (SELECT name FROM sys.database_principals WHERE name = '%s') " +
                                    "BEGIN CREATE USER [%s] FOR LOGIN [%s]; END",
                                    dbUser, dbUser, dbUser
                            ));

                            // Re-asignar roles (por si fueron revocados)
                            existingDbStmt.execute(String.format(
                                    "IF NOT EXISTS (SELECT 1 FROM sys.database_role_members rm " +
                                    "JOIN sys.database_principals u ON rm.member_principal_id = u.principal_id " +
                                    "JOIN sys.database_principals r ON rm.role_principal_id = r.principal_id " +
                                    "WHERE u.name = '%s' AND r.name = 'db_datareader') " +
                                    "BEGIN ALTER ROLE db_datareader ADD MEMBER [%s]; END",
                                    dbUser, dbUser
                            ));

                            existingDbStmt.execute(String.format(
                                    "IF NOT EXISTS (SELECT 1 FROM sys.database_role_members rm " +
                                    "JOIN sys.database_principals u ON rm.member_principal_id = u.principal_id " +
                                    "JOIN sys.database_principals r ON rm.role_principal_id = r.principal_id " +
                                    "WHERE u.name = '%s' AND r.name = 'db_datawriter') " +
                                    "BEGIN ALTER ROLE db_datawriter ADD MEMBER [%s]; END",
                                    dbUser, dbUser
                            ));

                            existingDbStmt.execute(String.format(
                                    "IF NOT EXISTS (SELECT 1 FROM sys.database_role_members rm " +
                                    "JOIN sys.database_principals u ON rm.member_principal_id = u.principal_id " +
                                    "JOIN sys.database_principals r ON rm.role_principal_id = r.principal_id " +
                                    "WHERE u.name = '%s' AND r.name = 'db_ddladmin') " +
                                    "BEGIN ALTER ROLE db_ddladmin ADD MEMBER [%s]; END",
                                    dbUser, dbUser
                            ));

                            existingDbStmt.execute(String.format("GRANT VIEW DEFINITION ON DATABASE::[%s] TO [%s]", existingDb, dbUser));
                            existingDbStmt.execute(String.format("DENY ALTER ON DATABASE::[%s] TO [%s]", existingDb, dbUser));
                        }
                    } catch (Exception e) {
                        // Ignorar si la BD ya no existe o hay problemas
                        log.warn("Could not grant permissions on existing database {}: {}", existingDb, e.getMessage());
                    }
                }
            }
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

    private void updateDatabaseAccess(Engine engine, String dbName, String dbUser, Instance.Status status) {
        try {
            String engineName = engine.getName().toLowerCase();
            EngineDetails conf = getEngineConfig(engineName);
            System.out.println("hola " + conf.getHost() + conf.getPort() + conf.getRootUser() + conf.getRootPassword());

            switch (engineName) {
                case "mysql" -> updateMySQLAccess(conf, dbName, dbUser, status);
                case "postgres" -> updatePostgresAccess(conf, dbName, dbUser, status);
                case "sqlserver" -> updateSQLServerAccess(conf, dbName, dbUser, status);
                case "mongodb" -> updateMongoDBAccess(conf, dbName, dbUser, status);
                case "cassandra" -> updateCassandraAccess(conf, dbName, dbUser, status);
                case "redis" -> updateRedisAccess(conf, dbUser, status);
                default -> throw new UnsupportedOperationException("Engine not supported: " + engineName);
            }

        } catch (Exception e) {
            log.error("Error updating access for {}: {}", engine.getName(), e.getMessage(), e);
            throw new DatabaseOperationException("Error updating access for " + engine.getName() + ": " + e.getMessage(), e);
        }
    }

    private void updateMySQLAccess(EngineDetails conf, String dbName, String dbUser, Instance.Status status) throws Exception {
        String url = "jdbc:mysql://" + conf.getHost() + ":" + conf.getPort() + "/?allowPublicKeyRetrieval=true&useSSL=false";
        try (Connection conn = DriverManager.getConnection(url, conf.getRootUser(), conf.getRootPassword());
             Statement stmt = conn.createStatement()) {

            if (status == Instance.Status.SUSPENDED) {
                // Revocar todos los permisos de la base de datos
                stmt.execute("REVOKE ALL PRIVILEGES ON " + dbName + ".* FROM '" + dbUser + "'@'%'");
                stmt.execute("FLUSH PRIVILEGES");
            } else if (status == Instance.Status.RUNNING) {
                // Restaurar permisos de DML y algunos DDL (sin DROP, ALTER, RENAME)
                stmt.execute("GRANT SELECT, INSERT, UPDATE, DELETE, CREATE, INDEX, SHOW VIEW, CREATE VIEW ON " + dbName + ".* TO '" + dbUser + "'@'%'");
                stmt.execute("FLUSH PRIVILEGES");
            }
        }
    }

    private void updatePostgresAccess(EngineDetails conf, String dbName, String dbUser, Instance.Status status) throws Exception {
        String url = "jdbc:postgresql://" + conf.getHost() + ":" + conf.getPort() + "/postgres";
        try (Connection conn = DriverManager.getConnection(url, conf.getRootUser(), conf.getRootPassword());
             Statement stmt = conn.createStatement()) {

            if (status == Instance.Status.SUSPENDED) {
                // Revocar permiso de conexión a la base de datos
                stmt.execute("REVOKE CONNECT ON DATABASE " + dbName + " FROM " + dbUser);

                // Terminar todas las conexiones activas del usuario a esta base de datos
                stmt.execute("SELECT pg_terminate_backend(pid) FROM pg_stat_activity " +
                        "WHERE datname = '" + dbName + "' AND usename = '" + dbUser + "'");
            } else if (status == Instance.Status.RUNNING) {
                // Restaurar permiso de conexión
                stmt.execute("GRANT CONNECT ON DATABASE " + dbName + " TO " + dbUser);
            }
        }

        // Si está activando (RUNNING), restaurar permisos en el schema
        if (status == Instance.Status.RUNNING) {
            String dbUrl = "jdbc:postgresql://" + conf.getHost() + ":" + conf.getPort() + "/" + dbName;
            try (Connection dbConn = DriverManager.getConnection(dbUrl, conf.getRootUser(), conf.getRootPassword());
                 Statement dbStmt = dbConn.createStatement()) {

                // Restaurar permisos en el schema public
                dbStmt.execute("GRANT USAGE, CREATE ON SCHEMA public TO " + dbUser);
                dbStmt.execute("GRANT SELECT, INSERT, UPDATE, DELETE ON ALL TABLES IN SCHEMA public TO " + dbUser);
                dbStmt.execute("GRANT USAGE, SELECT ON ALL SEQUENCES IN SCHEMA public TO " + dbUser);
            }
        }
    }

    private void updateSQLServerAccess(EngineDetails conf, String dbName, String dbUser, Instance.Status status) throws Exception {
        log.info("Updating SQL Server access - DB: {}, User: {}, Status: {}", dbName, dbUser, status);
        log.debug("SQL Server config - Host: {}, Port: {}, RootUser: {}", conf.getHost(), conf.getPort(), conf.getRootUser());

        // Conectarse primero a la base de datos master con las credenciales de root
        String masterUrl = "jdbc:sqlserver://" + conf.getHost() + ":" + conf.getPort() +
                ";databaseName=master;encrypt=true;trustServerCertificate=true";

        log.debug("Attempting to connect to master database: {}", masterUrl);

        try (Connection masterConn = DriverManager.getConnection(masterUrl, conf.getRootUser(), conf.getRootPassword());
             Statement masterStmt = masterConn.createStatement()) {

            log.info("Successfully connected to SQL Server master database");

            if (status == Instance.Status.SUSPENDED) {
                // Matar conexiones activas del usuario a la base de datos específica
                log.info("Killing active sessions for user {} in database {}", dbUser, dbName);
                masterStmt.execute(String.format(
                    "USE master; " +
                    "DECLARE @kill varchar(8000) = ''; " +
                    "SELECT @kill = @kill + 'KILL ' + CONVERT(varchar(5), session_id) + ';' " +
                    "FROM sys.dm_exec_sessions " +
                    "WHERE login_name = '%s' AND database_id = DB_ID('%s'); " +
                    "EXEC(@kill);",
                    dbUser, dbName
                ));
            }
        } catch (Exception e) {
            log.error("Failed to connect to SQL Server master database: {}", e.getMessage(), e);
            throw new RuntimeException("Cannot connect to SQL Server master database. Please verify credentials: " + e.getMessage(), e);
        }

        // Ahora conectarse a la base de datos específica para cambiar permisos
        String dbUrl = "jdbc:sqlserver://" + conf.getHost() + ":" + conf.getPort() +
                ";databaseName=" + dbName + ";encrypt=true;trustServerCertificate=true";

        log.debug("Attempting to connect to database: {}", dbUrl);

        try (Connection conn = DriverManager.getConnection(dbUrl, conf.getRootUser(), conf.getRootPassword());
             Statement stmt = conn.createStatement()) {

            log.info("Successfully connected to SQL Server database: {}", dbName);

            if (status == Instance.Status.SUSPENDED) {
                // Revocar todos los roles y permisos
                log.info("Suspending user {} - Removing roles and denying access", dbUser);
                stmt.execute(String.format("ALTER ROLE db_datareader DROP MEMBER [%s]", dbUser));
                stmt.execute(String.format("ALTER ROLE db_datawriter DROP MEMBER [%s]", dbUser));
                stmt.execute(String.format("ALTER ROLE db_ddladmin DROP MEMBER [%s]", dbUser));

                // Denegar conexión
                stmt.execute(String.format("DENY CONNECT TO [%s]", dbUser));
                log.info("User {} suspended successfully", dbUser);

            } else if (status == Instance.Status.RUNNING) {
                // Restaurar permiso de conexión
                log.info("Activating user {} - Granting access and restoring roles", dbUser);
                stmt.execute(String.format("GRANT CONNECT TO [%s]", dbUser));

                // Restaurar roles
                stmt.execute(String.format("ALTER ROLE db_datareader ADD MEMBER [%s]", dbUser));
                stmt.execute(String.format("ALTER ROLE db_datawriter ADD MEMBER [%s]", dbUser));
                stmt.execute(String.format("ALTER ROLE db_ddladmin ADD MEMBER [%s]", dbUser));
                log.info("User {} activated successfully", dbUser);
            }
        } catch (Exception e) {
            log.error("Failed to update permissions in database {}: {}", dbName, e.getMessage(), e);
            throw new RuntimeException("Cannot update permissions in database " + dbName + ": " + e.getMessage(), e);
        }
    }

    private void updateMongoDBAccess(EngineDetails conf, String dbName, String dbUser, Instance.Status status) {
        String uri = "mongodb://" + conf.getRootUser() + ":" + conf.getRootPassword() + "@" + conf.getHost() + ":" + conf.getPort() + "/admin";
        try (MongoClient mongoClient = MongoClients.create(uri)) {
            MongoDatabase adminDb = mongoClient.getDatabase("admin");

            if (status == Instance.Status.SUSPENDED) {
                // Revocar roles del usuario
                adminDb.runCommand(new org.bson.Document("updateUser", dbUser)
                        .append("roles", java.util.List.of()));
            } else if (status == Instance.Status.RUNNING) {
                // Restaurar rol de lectura/escritura
                adminDb.runCommand(new org.bson.Document("updateUser", dbUser)
                        .append("roles", java.util.List.of(
                                new org.bson.Document("role", "readWrite").append("db", dbName)
                        )));
            }
        }
    }

    private void updateCassandraAccess(EngineDetails conf, String keyspace, String dbUser, Instance.Status status) {
        try (CqlSession session = CqlSession.builder()
                .addContactPoint(new InetSocketAddress(conf.getHost(), conf.getPort()))
                .withAuthCredentials(conf.getRootUser(), conf.getRootPassword())
                .withLocalDatacenter("datacenter1")
                .build()) {

            if (status == Instance.Status.SUSPENDED) {
                // Revocar todos los permisos en el keyspace
                session.execute("REVOKE ALL PERMISSIONS ON KEYSPACE " + keyspace + " FROM " + dbUser);
            } else if (status == Instance.Status.RUNNING) {
                // Restaurar permisos
                session.execute("GRANT ALL PERMISSIONS ON KEYSPACE " + keyspace + " TO " + dbUser);
            }
        }
    }

    private void updateRedisAccess(EngineDetails conf, String dbUser, Instance.Status status) {
        try (Jedis jedis = new Jedis(conf.getHost(), conf.getPort())) {
            jedis.auth(conf.getRootPassword());

            if (status == Instance.Status.SUSPENDED) {
                // Deshabilitar el usuario
                jedis.aclSetUser("user_" + dbUser, "off");
            } else if (status == Instance.Status.RUNNING) {
                // Habilitar el usuario
                jedis.aclSetUser("user_" + dbUser, "on");
            }
        }
    }

    // TODO: Get statistics - databases created today
    public ResponseEntity<DatabaseStatsResponse> getDatabasesCreatedToday() {
        try {
            LocalDate today = LocalDate.now();
            LocalDateTime startOfDay = today.atStartOfDay();
            LocalDateTime endOfDay = today.atTime(LocalTime.MAX);

            Long count = instanceRepository.countByCreatedAtBetween(startOfDay, endOfDay);

            DatabaseStatsResponse response = DatabaseStatsResponse.builder()
                    .date(today)
                    .count(count)
                    .build();

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Error getting databases created today: {}", e.getMessage(), e);
            throw new DatabaseOperationException("Failed to retrieve today's database statistics: " + e.getMessage());
        }
    }

    // TODO: Get statistics - total databases
    public ResponseEntity<DatabaseStatsResponse> getTotalDatabases() {
        try {
            Long totalCount = instanceRepository.countTotal();
            LocalDate today = LocalDate.now();

            DatabaseStatsResponse response = DatabaseStatsResponse.builder()
                    .date(today)
                    .count(totalCount)
                    .build();

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Error getting total databases: {}", e.getMessage(), e);
            throw new DatabaseOperationException("Failed to retrieve total database statistics: " + e.getMessage());
        }
    }

    // TODO: Get statistics - databases by engine
    public ResponseEntity<List<DatabaseStatsByEngineResponse>> getDatabasesByEngine() {
        try {
            List<Object[]> results = instanceRepository.countByEngine();

            List<DatabaseStatsByEngineResponse> responses = results.stream()
                    .map(result -> DatabaseStatsByEngineResponse.builder()
                            .engineName((String) result[0])
                            .count((Long) result[1])
                            .build())
                    .collect(Collectors.toList());

            return ResponseEntity.ok(responses);
        } catch (Exception e) {
            log.error("Error getting databases by engine: {}", e.getMessage(), e);
            throw new DatabaseOperationException("Failed to retrieve database statistics by engine: " + e.getMessage());
        }
    }

}

