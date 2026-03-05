pipeline {
  agent any

  options {
    timestamps()
    disableConcurrentBuilds()
  }

  environment {
    DB_NAME  = 'calc_data'
    DB_USER  = 'app_user'
    DB_TABLE = 'calc_results'

    DB_SERVICE  = 'db'
    APP_SERVICE = 'app'
  }

  stages {

    stage('Checkout') {
      steps {
        checkout scm
      }
    }

    stage('Maven: build & test') {
      steps {
        sh '''
          set -e
          mvn -B -U clean test package
        '''
      }
    }

    stage('Compose: up (db + app)') {
      steps {
        sh '''
          set -e
          docker compose version
          docker compose down -v || true
          docker compose up --build -d
          docker compose ps
        '''
      }
    }

    stage('Verify: DB + table exists') {
      steps {
        sh '''
          set -e

          # Yritetään löytää salasana db-containerin envistä.
          DB_PASS=$(docker compose exec -T ${DB_SERVICE} /bin/sh -lc 'echo "${MARIADB_PASSWORD:-${MYSQL_PASSWORD:-${MARIADB_ROOT_PASSWORD:-${MYSQL_ROOT_PASSWORD:-}}}"' | tr -d '\r')

          if [ -z "$DB_PASS" ]; then
            echo "ERROR: Could not detect DB password from ${DB_SERVICE} environment."
            echo "Fix: set MARIADB_PASSWORD (or MYSQL_PASSWORD) in docker-compose.yml for the db service."
            exit 1
          fi

          echo "Waiting DB to accept connections..."
          for i in $(seq 1 30); do
            if docker compose exec -T ${DB_SERVICE} mariadb -u${DB_USER} -p${DB_PASS} -e "SELECT 1" >/dev/null 2>&1; then
              echo "DB is up."
              break
            fi
            sleep 2
            if [ "$i" -eq 30 ]; then
              echo "ERROR: DB did not become ready in time."
              docker compose logs ${DB_SERVICE} || true
              exit 1
            fi
          done

          echo "Checking database + table..."
          docker compose exec -T ${DB_SERVICE} mariadb -u${DB_USER} -p${DB_PASS} -e "SHOW DATABASES LIKE '${DB_NAME}';"
          docker compose exec -T ${DB_SERVICE} mariadb -u${DB_USER} -p${DB_PASS} -e "USE ${DB_NAME}; SHOW TABLES LIKE '${DB_TABLE}';"
          docker compose exec -T ${DB_SERVICE} mariadb -u${DB_USER} -p${DB_PASS} -e "USE ${DB_NAME}; DESCRIBE ${DB_TABLE};"
          docker compose exec -T ${DB_SERVICE} mariadb -u${DB_USER} -p${DB_PASS} -e "USE ${DB_NAME}; SELECT * FROM ${DB_TABLE} ORDER BY id DESC LIMIT 5;"
        '''
      }
    }
  }

  post {
    always {
      sh '''
        set +e
        docker compose logs --no-color --tail=200 ${APP_SERVICE} || true
        docker compose logs --no-color --tail=200 ${DB_SERVICE} || true
        docker compose down -v || true
      '''
    }
  }
}