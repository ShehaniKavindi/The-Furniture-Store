# The Furniture Store ERD

```mermaid
erDiagram
    USER ||--o{ ADDRESS : has
    USER ||--o{ CART : owns
    USER ||--o{ WISHLIST : saves
    USER ||--o{ ORDERS : places
    STATUS ||--o{ USER : classifies
    ROLE ||--o{ ADMIN : authorises
    STATUS ||--o{ ADMIN : classifies
    CATEGORY ||--o{ PRODUCT : contains
    PRODUCT ||--o{ PRODUCT_IMAGES : has
    PRODUCT ||--o{ CART : appears_in
    PRODUCT ||--o{ WISHLIST : appears_in
    ORDERS ||--o{ ORDERD_ITEMS : contains
    PRODUCT ||--o{ ORDERD_ITEMS : ordered_as
    ORDERS ||--o{ DELIVERY : has
    DELIVERY_TYPES ||--o{ DELIVERY : defines
    STATUS ||--o{ DELIVERY : tracks
    PROVINCE ||--o{ DISTRICT : contains
    DISTRICT ||--o{ CITY : contains
    CITY ||--o{ ADDRESS : locates
```

The design keeps repeating reference data separate (statuses, roles, categories and locations) and resolves the many-to-many user-product relationships through `cart` and `wishlist` tables.
