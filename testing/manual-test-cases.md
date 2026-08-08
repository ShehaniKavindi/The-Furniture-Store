# Manual Test Cases

| ID | Feature | Steps | Expected result |
|---|---|---|---|
| TC-01 | Registration validation | Submit missing/invalid values | Field-level error response; no account created |
| TC-02 | Login and remember me | Log in with valid credentials and select Remember Me | Session starts; login persists after browser restart |
| TC-03 | Access control | Request an admin API as a customer | API returns an unauthorised response |
| TC-04 | Wishlist | Add, list, then remove a product | Wishlist state changes correctly for the logged-in user |
| TC-05 | Advanced search | Search using category, price, stock, and page controls | Only matching products appear; page controls navigate results |
| TC-06 | Product upload | Upload valid image; then oversized/invalid file | Valid image saves; invalid file is rejected with a clear message |
| TC-07 | Checkout | Add product to cart and place an order | Order and delivery record are created; stock is updated |
| TC-08 | Reports | Open Reports and export category sales | KPI tables load; CSV downloads |
| TC-09 | Multimedia | Open About page and use video/audio controls | Media controls are visible and playable |
| TC-10 | Error handling | Request a nonexistent page | Friendly 404 page is displayed |
