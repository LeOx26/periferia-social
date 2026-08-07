#!/bin/bash
# Cada microservicio es dueño de su propia base: no hay joins ni claves foráneas
# entre ellas. Postgres ejecuta este script solo en el primer arranque del volumen.
set -e

# --dbname es obligatorio: sin él psql intenta conectarse a una base con el mismo
# nombre que el usuario, que no existe.
psql -v ON_ERROR_STOP=1 --username "$POSTGRES_USER" --dbname "$POSTGRES_DB" <<-EOSQL
    CREATE DATABASE authdb;
    CREATE DATABASE socialdb;
EOSQL
