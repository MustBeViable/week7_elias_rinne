pipeline {
  agent any

  options {
    timestamps()
    disableConcurrentBuilds()
  }

  environment {
    // Jenkins (macOS) ei peri sun shellin PATHia → pakotetaan
    PATH = "/opt/homebrew/bin:/usr/local/bin:/usr/bin:/bin:/usr/sbin:/sbin"

    DB_NAME  = "calc_data"
    DB_USER  = "app_user"
    DB_PASS  = "STRONG_PASSWORD"   // tämän pitää täsmätä docker-compose.yml:ssä
    DB_TABLE = "calc_results"

    DB_SERVICE = "db"
  }

  stages {

    stage('Checkout') {
      steps { checkout scm }
    }

    stage('Tool check') {
      steps {
        sh '''
          set -e
          whoami
          echo "PATH=$PATH"
          which mvn
          mvn -v
          which docker
          docker version
          docker compose version
        '''
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

    stage('Compose: up (db only)') {
      steps {
        sh '''
          set -e
          docker compose down -v || true
          docker compose up --build -d ${DB_SERVICE}
          docker compose ps
        '''
      }
    }

    stage('Verify: DB + table exists') {
      steps {
        sh '''
          set -e

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

          echo "Show DB and table:"
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
        docker compose logs --no-color --tail=200 ${DB_SERVICE} || true
        docker compose down -v || true
      '''
    }
  }
}