Online Banking Management System – Java (Swing + SQLite)
👥 Team: 404Error

Prashant Upadhyay — Team Leader

Avnish Kumar — Team Member

Abhinav Yadav — Team Member

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
1. Compile
javac BankSystemSQLite.java

2. Run
java BankSystemSQLite


A file bankdb.db will be generated automatically.

🔐 Security

SHA-256 hashing

Random salt per user

Input validation

Role-based access

💡 Future Enhancements

OTP-based login

PDF mini statement

Loan module

Dark mode UI

🤝 Contributing

Feel free to submit issues or pull requests.

📜 License

This project is free for educational and academic use.
