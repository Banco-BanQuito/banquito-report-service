# banquito-report-service

Microservicio de reportes y comprobantes para BanQuito V2, responsable de generar el Reporte de Novedades y el Comprobante de Liquidacion de un lote procesado por `routing-service`.

Este servicio pertenece a Anthony y forma parte del sistema Switch de Pagos Masivos.

## Que hace

- Lee los lotes procesados por Paul desde MongoDB `routingdb`.
- Genera un CSV descargable con el Reporte de Novedades.
- Genera un JSON con el Comprobante de Liquidacion.
- Registra metadatos de reportes en `payment_report`.
- Registra auditoria de notificaciones en `beneficiary_notification`.
- Consume `notification-service` por gRPC para notificar beneficiarios.
- Expone endpoints REST para que Kong Switch y el frontend Empresas los consuman.

## Puertos

| Uso | Puerto | Descripcion |
| --- | --- | --- |
| REST | `8088` | Endpoints de reportes y health |

Este servicio si debe exponerse por Kong Switch para el frontend Empresas.

## Endpoints REST

### Health

```http
GET http://localhost:8088/api/v2/reports/health
```

Respuesta esperada:

```json
{
  "status": "UP",
  "service": "report-service",
  "version": "2.0"
}
```

### Reporte de Novedades

```http
GET http://localhost:8088/api/v2/payments/batches/{batchId}/report
```

Respuesta:

- `Content-Type: text/csv`
- Header de descarga `attachment`
- Archivo CSV con columnas:

```text
LINE_NUMBER,TRANSACTION_ID,BENEFICIARY_NAME,BENEFICIARY_ID,DESTINATION_ACCOUNT,AMOUNT,STATUS,ERROR_CODE,ERROR_DESCRIPTION,PROCESSED_AT
```

El servicio lee:

- `payment_detail`
- `detail_status_log`
- `batch_status_log`

Si existen logs de estado/error, se usan para enriquecer el CSV.

### Comprobante de Liquidacion

```http
GET http://localhost:8088/api/v2/payments/receipts/{batchId}
```

Respuesta esperada:

```json
{
  "batchId": "uuid-lote",
  "clientRuc": "0912345678",
  "companyName": "Empresa ABC",
  "processedDate": "2026-05-30",
  "totalRecords": 100,
  "successful": 97,
  "rejected": 3,
  "totalAmountDispatched": 82450.00,
  "commissionCharged": 58.20,
  "ivaCharged": 8.73,
  "totalDebited": 82516.93,
  "receiptUuid": "uuid-recibo",
  "generatedAt": "2026-05-30T14:05:00Z"
}
```

Regla importante:

- Si existe `payment_batch`, el comprobante solo se genera cuando `status=COMPLETED`.
- Si el lote existe pero no esta `COMPLETED`, responde `409 Conflict`.
- Si no hay detalles para el lote, responde `404 Not Found`.

## MongoDB

Base usada:

```text
routingdb
```

Colecciones leidas de Paul:

| Coleccion | Uso |
| --- | --- |
| `payment_batch` | Estado general del lote, empresa, totales si existen |
| `payment_detail` | Detalle de cada linea procesada |
| `detail_status_log` | Ultimo estado/error por detalle |
| `batch_status_log` | Logs alternativos de estado si Paul los guarda ahi |

Colecciones escritas por Anthony:

| Coleccion | Uso |
| --- | --- |
| `payment_report` | Metadata del reporte o comprobante generado |
| `beneficiary_notification` | Resultado de notificaciones solicitadas desde report-service |

## Integracion con Paul

Paul debe escribir los lotes en MongoDB `routingdb`. Este servicio no requiere cambios en `routing-service`; solo lee las colecciones existentes.

Campos soportados para buscar lote/detalles:

- `_id`
- `batch_id`
- `payment_batch_id`

Campos soportados para logs de detalle:

- `payment_detail_id`
- `paymentDetailId`
- `detail_id`
- `detailId`

Campos soportados para estado/error:

- `status`
- `new_status`
- `current_status`
- `error_code`
- `errorCode`
- `code`
- `error_description`
- `errorDescription`
- `message`
- `reason`

Esto se hizo asi para tolerar pequenas diferencias de nombres mientras Paul termina su servicio.

## Integracion con notification-service

`report-service` llama por gRPC a:

```text
notification-service:9092
```

Contrato compatible:

```text
src/main/proto/notification.proto
```

Variables:

```env
NOTIFICATION_GRPC_HOST=notification-service
NOTIFICATION_GRPC_PORT=9092
NOTIFICATION_GRPC_ENABLED=true
```

Para levantar `report-service` sin `notification-service`, usar:

```env
NOTIFICATION_GRPC_ENABLED=false
```

## Variables de entorno

| Variable | Valor local recomendado | Valor Docker/infra | Descripcion |
| --- | --- | --- | --- |
| `SERVER_PORT` | `8088` | `8088` | Puerto REST |
| `MONGODB_URI` | `mongodb://localhost:27017/routingdb` | `mongodb://mongo:27017/routingdb` | Conexion a MongoDB |
| `REPORT_STORAGE_PATH` | `./reports` | `/app/reports` o volumen | Carpeta donde se guardan CSV generados |
| `NOTIFICATION_GRPC_HOST` | `localhost` | `notification-service` | Host gRPC de notificaciones |
| `NOTIFICATION_GRPC_PORT` | `9092` | `9092` | Puerto gRPC de notificaciones compatible con routing-service |
| `NOTIFICATION_GRPC_ENABLED` | `true` o `false` | `true` | Activa llamada gRPC |

## Como levantar localmente

Requisitos:

- Java 21
- Maven 3.9+
- MongoDB con `routingdb`
- Opcional: `notification-service` levantado en `9092`

Levantar sin notificaciones:

```bash
mvn spring-boot:run -Dspring-boot.run.arguments=--banquito.notification.grpc-enabled=false
```

Levantar con notificaciones:

```bash
$env:NOTIFICATION_GRPC_HOST="localhost"
$env:NOTIFICATION_GRPC_PORT="9092"
$env:NOTIFICATION_GRPC_ENABLED="true"
mvn spring-boot:run
```

Si estas usando Mongo desde `banquito-infra` local:

```bash
$env:MONGODB_URI="mongodb://localhost:27017/routingdb"
mvn spring-boot:run
```

## Como levantar con Docker

Construir imagen:

```bash
docker build -t banquito-report-service .
```

Ejecutar apuntando a Mongo local y notification local:

```bash
docker run --rm -p 8088:8088 ^
  -e MONGODB_URI=mongodb://host.docker.internal:27017/routingdb ^
  -e NOTIFICATION_GRPC_HOST=host.docker.internal ^
  -e NOTIFICATION_GRPC_PORT=9092 ^
  -e NOTIFICATION_GRPC_ENABLED=true ^
  banquito-report-service
```

En `banquito-infra`, este servicio debe usar:

```env
MONGODB_URI=mongodb://mongo:27017/routingdb
REPORT_STORAGE_PATH=/app/reports
NOTIFICATION_GRPC_HOST=notification-service
NOTIFICATION_GRPC_PORT=9092
NOTIFICATION_GRPC_ENABLED=true
```

## Rutas esperadas en Kong Switch

Estas rutas deben apuntar a `report-service:8088`:

```text
GET /api/v2/payments/batches/{batchId}/report
GET /api/v2/payments/receipts/{batchId}
GET /api/v2/reports/health
```

`notification-service` no debe exponerse por Kong.

## Verificacion rapida

Compilar y correr tests:

```bash
mvn test
```

Probar health:

```bash
curl http://localhost:8088/api/v2/reports/health
```

Descargar reporte:

```bash
curl -OJ http://localhost:8088/api/v2/payments/batches/{batchId}/report
```

Generar comprobante:

```bash
curl http://localhost:8088/api/v2/payments/receipts/{batchId}
```

## Flujo completo esperado

1. Alan recibe y publica el archivo de pagos.
2. Paul procesa el lote y escribe en `routingdb`.
3. Paul marca el lote como `COMPLETED`.
4. Frontend Empresas consulta el estado por el servicio de Paul.
5. Cuando el lote esta completo, Frontend Empresas llama a `report-service`.
6. `report-service` genera CSV de novedades o comprobante.
7. `report-service` registra metadata en `payment_report`.
8. Si corresponde, `report-service` llama a `notification-service` por gRPC.
9. `notification-service` envia o simula correo y registra auditoria en `beneficiary_notification`.

## Notas para el equipo

- Este servicio no crea ni modifica las colecciones de Paul, solo las lee.
- El comprobante depende de que Paul deje el lote en `COMPLETED`.
- Si `notification-service` no esta listo, desactivar `NOTIFICATION_GRPC_ENABLED`.
- No guardar archivos generados ni credenciales en Git.
- Los CSV se guardan en `REPORT_STORAGE_PATH` y tambien se devuelven como descarga.
