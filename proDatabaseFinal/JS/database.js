import { createConnection } from 'mysql2';

// Create a connection to the database
const connection = createConnection({
    host: '127.0.0.1', // Replace with your database host
    user: 'Isaac',      // Replace with your database username
    password: '0000',      // Replace with your database password
    database: 'databasepro' // Replace with your database name
});

// Connect to the database
connection.connect((err) => {
    if (err) {
        console.error('Error connecting to the database:', err.stack);
        return;
    }
    console.log('Connected to the database as ID', connection.threadId);
});


// Close the connection when done
connection.end((err) => {
    if (err) {
        console.error('Error closing the connection:', err.stack);
        return;
    }
    console.log('Database connection closed.');
});


function getStudents() {
    return new Promise((resolve, reject) => {
        connection.query('SELECT * FROM students', (err, results) => {
            if (err) {
                console.error('Error executing query:', err.stack);
                reject(err);
                return;
            }
            console.log('Query results:', results);
            resolve(results);
        });
    });
}

