# 🛋️ The Furniture Store

A full-featured Java-based e-commerce application for buying and selling furniture online. Built with a modern REST API architecture using Jersey (JAX-RS), Hibernate ORM, and MySQL database.

## 🎯 Overview

The Furniture Store is a comprehensive e-commerce platform that allows users to:
- Browse and purchase furniture items
- Manage shopping carts
- Place and track orders
- Manage user profiles and addresses
- Administrators to manage products, categories, orders, and users
- Advanced search and filtering capabilities

The application features a robust REST API built with Jersey (JAX-RS), server-side session management, and secure payment integration with PayHere.

## ✨ Features

### For Customers
- **User Authentication**: Registration, login, email verification
- **Product Browsing**: Browse products by category, view detailed product information
- **Advanced Search**: Filter products by multiple criteria
- **Shopping Cart**: Add, update, and remove items from cart
- **Order Management**: Place orders, track order status, cancel orders
- **User Profile**: Update personal information and delivery addresses
- **Payment Integration**: Secure payment processing via PayHere
- **Location Services**: Province, district, and city selection

### For Administrators
- **Admin Authentication**: Secure admin login
- **Dashboard**: View key metrics and analytics
- **Product Management**: Add, update, and delete products with image uploads
- **Category Management**: Manage product categories
- **User Management**: View all customers, update customer status
- **Admin Management**: Add new admins, block/deactivate admins
- **Order Management**: View all orders, update order status
- **Inventory Tracking**: Monitor product quantities

### General Features
- **Responsive Design**: Modern web interface
- **Session Management**: Secure session handling with HTTP sessions
- **JSON Communication**: All API responses in JSON format
- **Image Upload Support**: Product images stored in webapp assets
- **Email Notifications**: Email template support for various events
- **Role-Based Access Control**: Different permissions for users and admins

## 🛠️ Technology Stack

| Technology | Purpose | Version |
|---|---|---|
| **Java** | Programming Language | 11 |
| **Jersey (JAX-RS)** | REST API Framework | 3.1.2 |
| **Hibernate ORM** | Database ORM | 6.1.7.Final |
| **MySQL** | Database | - |
| **MySQL Connector** | Database Driver | 9.0.0 |
| **Jakarta Servlet** | Servlet API | 6.0.0 |
| **Tomcat** | Web Server | 10.1.7 |
| **GSON** | JSON Processing | 2.10.1 |
| **Jakarta Mail** | Email Support | 2.0.2 |
| **Maven** | Build Tool | - |

