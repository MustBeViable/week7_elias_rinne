pipeline {
  agent any

  options {
    timestamps()
    disableConcurrentBuilds()
  }

  environment {
    // Jenkins (macOS) ei peri sun shellin PATHia → pakotetaan
    PATH = "/opt/homebrew/bin:/usr/local/bin:/usr/bin:/bin:/usr/sbin:/sbin"

    DB_NAME    = "calc_data"
    DB_USER    = "app_user"
    DB_PASS    = "STRONG_PASSWORD"
    DB_TABLE   = "calc_results"
    DB_SERVICE = "db"
    APP_SERVICE = "app"
  }

  stages {

    stage('Checkout') {
      steps {
        checkout scm
      }
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

    stage('Compose: up (db + app)') {
      steps {
        sh '''
          set -e
          docker compose down -v || true
          docker compose up --build -d
          docker compose ps
        '''
      }
    }

    stage('Verify: DB ready') {
      steps {
        sh '''
          set -e
          echo "Waiting DB to accept connections..."
          for i in $(seq 1 40); do
            if docker compose exec -T ${DB_SERVICE} mariadb -u${DB_USER} -p${DB_PASS} -e "SELECT 1" >/dev/null 2>&1; then
              echo "DB is ready."
              exit 0
            fi
            sleep 2
            echo "  ...still waiting (${i}/40)"
          done

          echo "ERROR: DB did not become ready in time."
          docker compose logs ${DB_SERVICE} || true
          exit 1
        '''
      }
    }

    stage('Verify: DB + table exists') {
      steps {
        sh '''
          set -e
          echo "Checking database exists: ${DB_NAME}"
          docker compose exec -T ${DB_SERVICE} mariadb -u${DB_USER} -p${DB_PASS} -e "SHOW DATABASES LIKE '${DB_NAME}';"

          echo "Checking table exists: ${DB_NAME}.${DB_TABLE}"
          docker compose exec -T ${DB_SERVICE} mariadb -u${DB_USER} -p${DB_PASS} -D ${DB_NAME} -e "SHOW TABLES LIKE '${DB_TABLE}';"

          echo "Describe table:"
          docker compose exec -T ${DB_SERVICE} mariadb -u${DB_USER} -p${DB_PASS} -D ${DB_NAME} -e "DESCRIBE ${DB_TABLE};"
        '''
      }
    }

    stage('Verify: insert + sample results') {
      steps {
        sh '''
          set -e
          echo "Attempting to insert a sample row (edit columns if your init.sql differs)."

          # YRITETÄÄN yleisimmällä skeemalla:
          # calc_results(operation, a, b, result, created_at)
          # Jos sun taulussa on eri sarakkeet, DESCRIBE-stage kertoo ne → muokkaa INSERTiä vastaamaan.
          docker compose exec -T ${DB_SERVICE} mariadb -u${DB_USER} -p${DB_PASS} -D ${DB_NAME} -e "
            INSERT INTO ${DB_TABLE} (operation, a, b, result)
            VALUES ('sum', 5, 7, 12);
          " || true

          echo "Sample rows:"
          docker compose exec -T ${DB_SERVICE} mariadb -u${DB_USER} -p${DB_PASS} -D ${DB_NAME} -e "SELECT * FROM ${DB_TABLE} ORDER BY 1 DESC LIMIT 20;"
        '''
      }
    }

    stage('App status') {
      steps {
        sh '''
          set -e
          echo "Compose status:"
          docker compose ps

          echo "Last app logs (for quick sanity):"
          docker compose logs --no-color --tail=80 ${APP_SERVICE} || true
        '''
      }
    }
  }

  post {
    always {
      sh '''
        set +e
        echo "==== docker compose ps ===="
        docker compose ps || true

        echo "==== db logs (tail) ===="
        docker compose logs --no-color --tail=200 ${DB_SERVICE} || true

        echo "==== app logs (tail) ===="
        docker compose logs --no-color --tail=200 ${APP_SERVICE} || true

        echo "==== cleanup ===="
        docker compose down -v || true
      '''
    }
  }
}