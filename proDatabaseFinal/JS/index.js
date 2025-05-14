
// Function to start the application
function start() {
    console.log("Application started.");
    const login = document.getElementById("login");
 

    login.addEventListener("click", function() {
        showLogin();
    });
    document.getElementById("home").addEventListener("click", function() {
        hideHome();
    });
}

function showLogin() {
    const login_div = document.getElementById("login_div");
    login_div.style.display = "block";
    login_div.style.marginBottom = "150px";
    const body = document.querySelector("body");
    body.style.backgroundColor = "rgba(83, 83, 62, 0.5)";
   

    document.getElementById("login_button").addEventListener("click", function() {
     
        const username = document.getElementById("username").value;
        const password = document.getElementById("password").value;

        if (username === "admin" && password === "admin") {
            alert("Login successful!");
            document.getElementById("login_div").style.display = "none";
          //  showMainMenu();
        } else {
            alert("Invalid username or password.");
        }
    });

    
}

function hideHome() {
    document.getElementById("login_div").style.display = "none";

}
// Function to close the application
function close() {
    console.log("Application closed.");
    
  
}

// Automatically start the application when the script is loaded
start();