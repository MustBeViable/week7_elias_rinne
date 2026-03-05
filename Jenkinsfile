stage('Verify: DB + table exists') {
  steps {
    sh '''
      set -e

      echo "Waiting DB to accept connections..."
      for i in $(seq 1 30); do
        if docker compose exec -T db mariadb -uapp_user -pSTRONG_PASSWORD -e "SELECT 1" >/dev/null 2>&1; then
          echo "DB is ready."
          break
        fi
        sleep 2
        if [ "$i" -eq 30 ]; then
          echo "ERROR: DB did not become ready in time."
          docker compose logs db
          exit 1
        fi
      done

      echo "Checking that table calc_results exists..."
      docker compose exec -T db mariadb -uapp_user -pSTRONG_PASSWORD -D calc_data -e "SHOW TABLES LIKE 'calc_results';"
    '''
  }
}