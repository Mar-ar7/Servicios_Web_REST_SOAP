# LogiTrack Distribution - REST API (Jakarta EE)

API REST para gestión de productos, clientes, órdenes, items y pagos de la empresa **LogiTrack Distribution**.

## Tecnologías

- Jakarta EE 10 (JAX-RS, JPA, CDI, JSON-B)
- WildFly / Payara / Glassfish (probado en WildFly 37+)
- PostgreSQL
- Maven

## Endpoints principales

- `/api/products`
- `/api/customers`
- `/api/orders`
- `/api/payments`
- `/api/reports/top-products`

Revisar las clases `*Resource` para ver cada operación CRUD y las consultas avanzadas
