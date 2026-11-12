# Gestión de pólizas - prueba técnica

API REST desarrollada en Java 21 y Spring Boot 3.4.5 para consultar, renovar y cancelar pólizas de arrendamiento y gestionar sus riesgos. El proyecto implementa los **seis endpoints** solicitados, una validación de `x-api-key`, un adaptador para enviar actualizaciones al CORE y un endpoint de simulación del CORE.

## Requisitos

- JDK 21
- Maven 3.9 o superior

## Ejecución

```bash
mvn clean spring-boot:run
```

Servidor: `http://localhost:8080`.

La aplicación carga datos de demostración al iniciar: póliza individual ID `1` y póliza colectiva ID `2`, con un riesgo en la primera y dos en la segunda. La base H2 es **en memoria** y se reinicia con la aplicación.

Para ejecutar pruebas automatizadas:

```bash
mvn test
```

## Autenticación

Todas las solicitudes requieren:

```http
x-api-key: 123456
```

En producción, se debe establecer `API_KEY` mediante una variable de entorno y reemplazar este esquema demostrativo por autenticación/autorización apropiada.

## API

| Método | Endpoint | Función |
|---|---|---|
| GET | `/polizas?tipo=COLECTIVA&estado=ACTIVA` | Filtrar pólizas (ambos filtros opcionales) |
| GET | `/polizas/{id}/riesgos` | Consultar riesgos asociados |
| POST | `/polizas/{id}/renovar` | Aplicar IPC y renovar vigencia |
| POST | `/polizas/{id}/cancelar` | Cancelar póliza y sus riesgos |
| POST | `/polizas/{id}/riesgos` | Agregar riesgo, solo para colectivas |
| POST | `/riesgos/{id}/cancelar` | Cancelar un riesgo |
| POST | `/core-mock/evento` | Simular la notificación al CORE |

### Ejemplos rápidos

```bash
curl -H 'x-api-key: 123456' 'http://localhost:8080/polizas?tipo=COLECTIVA'

curl -X POST 'http://localhost:8080/polizas/1/renovar' \
  -H 'x-api-key: 123456' -H 'Content-Type: application/json' \
  -d '{"ipc": 0.05}'

curl -X POST 'http://localhost:8080/polizas/2/riesgos' \
  -H 'x-api-key: 123456' -H 'Content-Type: application/json' \
  -d '{"arrendatario": "Laura Gómez", "inmueble": "Apartamento 301"}'

curl -X POST 'http://localhost:8080/polizas/2/cancelar' \
  -H 'x-api-key: 123456'

curl -X POST 'http://localhost:8080/core-mock/evento' \
  -H 'x-api-key: 123456' -H 'Content-Type: application/json' \
  -d '{"evento":"ACTUALIZACION","polizaId":555}'
```

### Contrato de renovación

El body recibe `ipc` como decimal entre `0` y `1`; por ejemplo, `0.05` corresponde al 5 %. La renovación comienza al día siguiente del fin de vigencia actual y conserva el mismo número de meses; recalcula el canon con `canon * (1 + ipc)` y la prima con `nuevoCanon * mesesVigencia`. Se redondea a dos decimales con `HALF_UP`.

### Errores esperados

- `400`: solicitud inválida, IPC incorrecto, campos faltantes o enums no válidos.
- `401`: API Key ausente o incorrecta.
- `404`: póliza o riesgo inexistente.
- `409`: infracción de reglas de negocio.
- `503`: falla en la comunicación con el CORE.

## Integración con CORE

La interfaz `CoreClient` desacopla la lógica de negocio del transporte HTTP. Su implementación `HttpCoreClient` realiza `POST` a `app.core.url` con el payload `{"evento":"ACTUALIZACION","polizaId":n}`; por defecto, apunta a `/core-mock/evento` dentro de la misma aplicación, que registra la operación en logs. Puede configurarse otro servicio mediante `CORE_URL`.

Las modificaciones usan transacciones locales y contactan al CORE antes de persistir cambios. Si la llamada falla, se devuelve `503` y no se modifica el estado local. **Limitación deliberada:** no existe una transacción distribuida entre la base y el CORE; una respuesta exitosa del CORE seguida de una falla en el commit local puede dejar sistemas inconsistentes. En producción usaría identificadores de operación, reintentos idempotentes, reconciliación y una estrategia outbox/saga acordada con el CORE.

## Decisiones de alcance

- Se implementan solo los endpoints que pide la prueba. La creación y edición genérica de pólizas y las notificaciones por correo/SMS se describen en el diseño de sistema, no se implementan en este ejercicio reducido.
- La póliza individual de demostración nace con un único riesgo; el endpoint de agregar riesgos solo permite pólizas colectivas.
- La cancelación repetida es idempotente.
- El esquema de seguridad con API Key es mínimo y responde a lo exigido por el enunciado.
- La base H2 y los datos iniciales se utilizan para simplificar la ejecución local.
- Para un entorno real, sería necesario guardar historial de renovaciones, disponer de auditoría, observabilidad centralizada y emplear una base persistente.

## Estructura

```text
src/main/java/com/evaluacion/polizas/
  controller/      Controladores REST y manejo de errores
  dto/             Contratos de entrada y salida
  integration/     Adaptador hacia CORE
  model/           Entidades y estados del dominio
  repository/      Persistencia JPA
  security/        Filtro de API Key
  service/         Reglas de negocio y transacciones
  config/          Datos de demostración
src/test/java/     Pruebas de negocio y HTTP
```

## Publicación en GitHub

Después de crear un repositorio público vacío llamado `gestion-polizas` en GitHub, ejecute en la raíz del proyecto:

```bash
git init
git add .
git commit -m "Implementar API de gestión de pólizas"
git branch -M main
git remote add origin https://github.com/TU_USUARIO/gestion-polizas.git
git push -u origin main
```

Sustituya `TU_USUARIO` por el usuario real de GitHub y verifique que el repositorio muestre el README, las carpetas `src` y el archivo `pom.xml`. El enlace público es el que debe figurar en la entrega de la prueba.
