Online Banking Management System – Java (Swing + SQLite)

A complete desktop-based Online Banking Management System built using Java Swing for UI and SQLite for backend storage.
The project includes secure login, user registration, admin panel, and full banking operations (deposit, withdraw, transfer, statement, account management).

🚀 Features
User Features

User Registration with:

Name, Email, Phone, Password

Password hashing + Salt for security

Login Authentication

View account balance

Deposit money

Withdraw money

Transfer to another account

View mini statement (last 10 transactions)

Logout

Admin Features

View all pending users

Approve user accounts

Freeze user accounts

View any user’s details

Full admin dashboard panel

🛠️ Technologies Used

Java (Swing GUI)

SQLite Database (lightweight, file-based)

JDBC (SQLite JDBC Driver)

SHA-256 password hashing

CardLayout for screen navigation

📂 Project Structure
BankSystemSQLite.java
└── Login Panel
└── Register Panel
└── User Dashboard
└── Admin Dashboard
└── UserDAO (Database operations)
└── AccountDAO
└── TransactionDAO
└── BankService
└── SQLite initialization

🧩 Database Structure (Auto-Created)
users

user_id (PK)

name

email (unique)

phone

role (ADMIN / CUSTOMER)

password_hash

salt

status (PENDING / ACTIVE / FROZEN)

accounts

acc_id (PK)

user_id (FK)

acc_type

balance

transactions

tx_id (PK)

acc_id (FK)

tx_type

amount

description

target_acc_id

▶️ How to Run
1. Download SQLite JDBC Driver (optional)

SQLite works without external setup.

2. Compile
javac BankSystemSQLite.java

3. Run
java BankSystemSQLite

4. A file bankdb.db will auto-generate

All tables will be created automatically.

🔐 Security Details

Uses SHA-256 hashing

Unique random salt per user

Prevents raw password storage

Input validation & role-based access

📝 Screens

Login Page

Registration Page

User Dashboard

Admin Panel

Transaction Dialog Boxes

💡 Future Enhancements

Add email OTP verification

Add PDF statement generation

Add loan management module

Add front-end themes (dark mode)

🤝 Contributing

Pull requests are welcome!
For major changes, open an issue first to discuss.

📜 License

This project is free to use for learning and academic purposes.
