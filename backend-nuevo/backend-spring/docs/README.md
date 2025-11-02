## 🧠 Sistema de Gestión de Consultorio Psicopedagógico

Este proyecto tiene como objetivo desarrollar un sistema completo para la gestión de un consultorio psicopedagógico.  
Consta de dos aplicaciones conectadas a un backend unificado:

- **Aplicación de Escritorio (JavaFX):** usada por la psicopedagoga y la secretaria.
- **Aplicación Web (React):** usada por los pacientes.
  El backend (Spring Boot) centraliza la lógica de negocio y se aloja en Render junto con la Base de Datos.

## 🧩 Arquitectura del Sistema

    A[Frontend Web (React)] -->|REST API| B[Backend (Spring Boot)]
    C[App de Escritorio (JavaFX)] -->|REST API| B
    B -->|PostgreSQL| D[(Base de Datos)]
    B -->|Resend API| E[Servicio de Emails]
    B -->|Mercado Pago API| F[Pagos] (Aún no tengo conocimiento de nada, no investigué)

### ⚙️ 3. **Tecnologías Utilizadas**

> Muy útil para que la IA reconozca entornos, frameworks y dependencias.

## ⚙️ Tecnologías Principales

**Backend:**

- Spring Boot 3.5.6
- JPA / Hibernate
- PostgreSQL
- Maven
- Render (deploy)

**Frontend Escritorio:**

- JavaFX 21

**Frontend Web:**

- React

**Servicios externos:**

- Resend API (envío de emails)
- Mercado Pago API (pagos)

## 🧠 Módulos Principales

### 🖥️ Aplicación de Escritorio (Administración)

- **Login y roles:** admin (único), psicopedagoga y secretaria, con control de permisos (admin y psicopedagoga todo, secretaria tiene restringuido módulos de pagos, estadísticas y usuarios).
- **Agenda:** visualización de horarios y días laborales de la semana,duración de cada sesión, descanso entre paciente y configuración de los mismos.
- **Turnos:** Lista de todos los proximos turnos, lista de los turnos pasados (para agregar su estado (confirmado (siempre, ya que es por defecto cuando sacan un turno) asistido, no asistido, cancelado por el paciente, cancelado por la psicopedagoga)), y historial (todos los turnos que ya pasaron y que ya tienen su estado actualizado)
- **Pacientes:** Lista de todos los pacientes con posibilidad de ABM completo (solo por parte de la psicopedagoa), ver ficha del paciente (información personal, realizar informe, hitorial de informes) .
- **Tareas:** Adjuntar documentos PDF o DOCX, poner fecha límite y estado de las tareas.
- **Pagos:** historial de pagos y sus detalles, y filtrado por pacientes
- **Estadísticas:** Gráficos estadísticos filtrados por cantidad de turnos atendidos (en un periodo específico), nuevos pacientes (en un periodo específico), asistencia vs ausenscia, ingresos (en un periodo específico), condición, género, nivel educativo, y mes de de mayor demanda (donde mas turnos se sacaron)
- **Usuarios:** ABM completo por todo tipo de usuario (psicopedagoga, secretaria)

### 🌐 Aplicación Web (Pacientes)

- **Registro y login de pacientes:** Que los pacientes se puedan registrar y ingresar al sistema
- **Agenda:** Que puedan seleccionar un turno en base a la disponibilidad de la agenda de la psicopedagoga
- **Turnos:** Que vea una lista (como un historial) de todos sus turnos y sus estados confirmado (siempre, ya que es por defecto cuando sacan un turno) asistido, no asistido, cancelado por el paciente, cancelado por la psicopedagoga. Ademas una sección de saldo a favor (en base a si el cancelo el turno con mas de 24hs de anticipación, tiene saldo a favor para sacar un proximo turno, sino, debe volver a pagar)
- **Tareas:** Visualización y descarga de materiales enviados por la psicopedagoga, además de una lista de todas las tareas y sus estados (entregada, no entregada)
- **Pagos:** Historial de todos sus pagos (de las sesiones, porque si no paga antes, no se reguistra el turno) con el detalle de la operación de Mercado Pago (y poder descargar el comprobante previnie te de mercado pago)
- **Recordatorios y confirmaciones por email (Resend API):** Mail de bienvenida cuando se registra al sistema, con un link para dar de alta su mail. Mail para recordar contraseña (tiene que estar verificado su mail para que se le mande el recordatorio). Y mail con el detalle de su turno (como fecha, hora y lugar)

## 🧾 Esquema de Base de Datos (simplificado)

- **usuarios**(id, actualizado_por, creado_por, fehca_actualizacion, fecha_creacion, activo, apellido, dni, email, email_validado, matricula, nombre, password, telefono, tipo, token expiracion, token_validacion)
- **pacientes**(id, actualizado_por, creado_por, fecha_actualizacion, fecha_creacion, antecedentes, condicion, contrato_aceptado, fecha_nacimiento, foto path, nivel_educativo, observaciones, usuario_id)
- **log_acciones**(id, tabla_afectada, accion, id_registro_aceptado, valores_anteriores, valores_nuevos, usuario_ejecutor, fecha_ejecucion, ip_origen)
- **configuracion_horaria**(id, dia_semana, activo, turno_manana, inicio_manana, fin_manana, turno_tarde, inicio_tarde, fin_tarde, duracion_sesion, tiempo_descanso, creado_por, actualizado_por, fecha_creacion, fecha_actualizacion)

ACA EL ENLACE DEL DIAGRAMA

Ver el diagrama en la carpeta [Diagrama de Entidad - Relacion](./docs/diagramas/Diagrama_de_Entidad_Relacion.png)

## 📦 Instalación y Ejecución

### Backend

run el ícono

### Frontend Escritorio

run el ícono

## 🧭 Próximos pasos

- [ ] Terminar módulo de Agenda
- [ ] Terminar módulo de turnos
- [ ] Terminar módulo de pacientes
- [ ] Terminar módulo de pagos
- [ ] Termianr módulo de estadísticas
- [ ] Termianr módulo de usuarios
- [ ] Termianr módulo de tareas
- [ ] Conectar módulo de pagos con Mercado Pago API
- [ ] Integrar envío de recordatorios automáticos (Resend) (falta compra de dominio)
- [ ] Sincronización entre escritorio y web
