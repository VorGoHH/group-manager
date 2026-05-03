# GROUP MANAGER — Контекст проекту для ШІ

## Що це за проект
Веб-система управління навчальною групою (241 н.г.) військового/курсантського типу.
Три основні функції: розхід (облік присутності), наряди, прибирання.

## Технічний стек
- **Backend:** Java Spring Boot 4.0.6, Spring Data JPA, Hibernate 7.2.12
- **База даних:** PostgreSQL 17
- **Frontend:** HTML/CSS/JavaScript (статичні файли в `src/main/resources/static/`)
- **IDE:** IntelliJ IDEA
- **Build:** Maven
- **Java:** 25
- **Пакет:** `group_manager`

## Структура пакетів
```
group_manager/
├── entity/           # JPA сутності
│   ├── Soldier.java
│   ├── Absence.java
│   ├── AbsenceReason.java (enum)
│   ├── Duty.java
│   ├── DutyRole.java
│   └── Cleaning.java
├── repository/       # Spring Data репозиторії
│   ├── SoldierRepository.java
│   ├── AbsenceRepository.java
│   ├── DutyRepository.java
│   ├── DutyRoleRepository.java
│   └── CleaningRepository.java
├── service/          # Бізнес логіка
│   ├── DutyService.java
│   └── CleaningService.java
├── controller/       # REST API
│   ├── SoldierController.java
│   ├── AbsenceController.java
│   ├── DutyController.java
│   └── CleaningController.java
└── DataInitializer.java  # Початкове заповнення БД
```

## База даних — таблиці

### soldiers
| Поле | Тип | Опис |
|------|-----|------|
| id | bigint | PK |
| first_name | varchar | Ім'я |
| last_name | varchar | Прізвище |
| middle_name | varchar | По батькові |
| rank | varchar | Звання |
| platoon | integer | Відділення (1/2/3) |
| is_commander | boolean | Командир відділення або групи |
| is_active | boolean | Активний |
| exclude_from_duty | boolean | Виключити з автонарядів |

### absences
| Поле | Тип | Опис |
|------|-----|------|
| id | bigint | PK |
| soldier_id | bigint | FK → soldiers |
| absence_date | date | Дата відсутності |
| reason | varchar | SICK/EXCUSED/BUSINESS_TRIP/INDIVIDUAL/ILLEGAL/ON_DUTY |
| note | varchar | Примітка |

### duty_roles
| Поле | Тип | Опис |
|------|-----|------|
| id | bigint | PK |
| name | varchar | Назва ролі |
| requires_commander | boolean | Чи потрібен командир |
| count_per_duty | integer | Кількість людей |

Ролі: `Черговий ПУ` (id=1), `Помічник ЧПУ` (id=2), `Днювальний` (id=3), `Їдальня` (id=4)

### duties
| Поле | Тип | Опис |
|------|-----|------|
| id | bigint | PK |
| duty_date | date | Дата наряду |
| soldier_id | bigint | FK → soldiers |
| role_id | bigint | FK → duty_roles |
| is_manual | boolean | Змінено вручну |
| duty_type | varchar | NORMAL або WITH_PU |

### cleanings
| Поле | Тип | Опис |
|------|-----|------|
| id | bigint | PK |
| cleaning_date | date | Дата прибирання |
| soldier_id | bigint | FK → soldiers |
| territory | varchar | "1 відділення" / "2 відділення" / "3 відділення" |
| is_manual | boolean | Змінено вручну |

## Особовий склад (24 особи)

### Командування (platoon=1, окремо від відділень)
| ПІБ | Звання | is_commander | exclude_from_duty |
|-----|--------|-------------|-------------------|
| Вашуленко Дмитро Андрійович | молодший сержант | true | false |
| Каніболоцький Максим Євгенович | сержант | false | **true** |

### 1 відділення (platoon=1)
| ПІБ | Звання | is_commander |
|-----|--------|-------------|
| Кіндратяк Вадим Вікторович | молодший сержант | **true** |
| Богаченко Павло Іванович | старший солдат | false |
| Демченок Дмитро Романович | солдат | false |
| Корчинський Володимир В'ячеславович | солдат | false |
| Кравченко Дмитро Вікторович | солдат | false |
| Марченко Андрій Миколайович | солдат | false |
| Лобач Віктор Михайлович | солдат | false |
| Сехін Нікіта Сергійович | солдат | false |

### 2 відділення (platoon=2)
| ПІБ | Звання | is_commander |
|-----|--------|-------------|
| Бондаренко Андрій Андрійович | солдат | **true** |
| Базелюк Олександр Вадимович | солдат | false |
| Вітвіцький Олег Вадимович | солдат | false |
| Кульбако Ростислав Сергійович | солдат | false |
| Радківський Владислав Миколайович | солдат | false |
| Тимашов Микита Денисович | солдат | false |
| Ходоровський Данило Андрійович | солдат | false |

### 3 відділення (platoon=3)
| ПІБ | Звання | is_commander |
|-----|--------|-------------|
| Рязанов Кирило Олександрович | солдат | **true** |
| Зубаков Дмитро Олександрович | солдат | false |
| Кінах Владислав Олександрович | солдат | false |
| Милетич Ростислав Олександрович | солдат | false |
| Мясоєдов Захар Арсенович | солдат | false |
| Снігир Артем Миколайович | солдат | false |
| Харченко Ярослав Павлович | солдат | false |

## Бізнес логіка

### Наряди — два типи

**NORMAL (звичайний):**
- 2 × Днювальний
- 1 × Їдальня

**WITH_PU (наряд з ПУ):**
- 1 × Черговий ПУ
- 1 × Помічник ЧПУ
- 2 × Днювальний
- 2 × Їдальня

### Правила призначення в наряд
- **Черговий ПУ:** тільки `is_commander=true` (Вашуленко, Кіндратяк, Бондаренко, Рязанов)
- **Помічник ЧПУ:** тільки Радківський, Ходоровський, Богаченко — також беруть участь у звичайних нарядах
- **Днювальний / Їдальня:** всі де `is_commander=false` і `exclude_from_duty=false`
- **Каніболоцький:** `exclude_from_duty=true` — в автогенерацію НЕ потрапляє, може бути призначений ТІЛЬКИ вручну через список кандидатів на Черговий ПУ
- **Алгоритм:** обирається той хто найменше разів чергував у цій ролі (`countBySoldierAndRole`)
- **Відсутні** (є в таблиці absences на цю дату) — не призначаються
- Одна дата — один наряд

### Прибирання
- Щодня по 2 особи з кожного відділення (всього 6 осіб)
- Тільки `is_commander=false` і `exclude_from_duty=false`
- Алгоритм: хто найменше разів прибирав (`countBySoldier`)

## REST API

### Розхід
```
GET    /api/attendance?date=YYYY-MM-DD
POST   /api/attendance/absent              body: {soldierId, date, reason, note}
DELETE /api/attendance/absent/{soldierId}?date=YYYY-MM-DD
```

### Солдати
```
GET  /api/soldiers
GET  /api/soldiers/{id}
PUT  /api/soldiers/{id}/status?isActive=true/false
```

### Наряди
```
GET    /api/duties?date=YYYY-MM-DD
POST   /api/duties/generate?date=YYYY-MM-DD&type=NORMAL|WITH_PU
DELETE /api/duties?date=YYYY-MM-DD
PUT    /api/duties/{id}/replace                    # замінити рандомно
PUT    /api/duties/{id}/replace/{soldierId}        # замінити конкретним
GET    /api/duties/{id}/candidates                 # список кандидатів
```

### Прибирання
```
GET    /api/cleaning?date=YYYY-MM-DD
POST   /api/cleaning/generate?date=YYYY-MM-DD
DELETE /api/cleaning?date=YYYY-MM-DD
PUT    /api/cleaning/{id}/replace/{soldierId}      # замінити вручну
GET    /api/cleaning/{id}/candidates               # список кандидатів
```

## Сторінки сайту
| URL | Файл | Опис |
|-----|------|------|
| `/` | `index.html` | Розхід — відмічати присутніх/відсутніх |
| `/duties.html` | `duties.html` | Наряди — генерація, перегляд, заміна |
| `/cleaning.html` | `cleaning.html` | Прибирання — генерація, перегляд, заміна |

## Конфігурація (application.yaml)
```yaml
spring:
  datasource:
    url: jdbc:postgresql://localhost:5432/group_manager
    username: postgres
    password: [ПАРОЛЬ]
    driver-class-name: org.postgresql.Driver
  jpa:
    hibernate:
      ddl-auto: update
    show-sql: true
```

## Що вже працює
- Повністю функціональний розхід з усіма причинами відсутності
- Генерація нарядів обох типів з алгоритмом рівномірного розподілу
- Заміна солдата рандомно і вручну зі списком кандидатів
- Прибирання по відділеннях з алгоритмом
- Навігація між усіма сторінками
- Доступ по локальній мережі (WiFi)

## Що ще не зроблено
- Деплой на Railway (зараз працює тільки локально)
- Історія нарядів і прибирань
- Статистика — скільки разів кожен ходив
- Адмін-панель для редагування списків ролей (помічники ЧПУ тощо)
