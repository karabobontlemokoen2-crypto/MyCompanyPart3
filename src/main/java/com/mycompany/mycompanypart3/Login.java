/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.mycompany.mycompanypart3;



/**
 *
 * @author Karabo Mkhize
 */
class Login {
    private String storedUsername;
    private String storedPassword;
        

    String registerUser(String username, String password, String cellPhoneNumber) {
        storedUsername = username;
        storedPassword = password;
        
    if (!checkPasswordComplexity(password)) {
        return "Password is not correctly formatted, please ensure that the password contains at least eight characters, a capital letter, a number, and a special character.";
    }
    if (!checkCellPhoneNumber(cellPhoneNumber)) {
        return "Cell phone number incorrectly formatted or does not contain international code.";
    }
    storedUsername = username;
    storedPassword = password;
    return "Username successfully captured. Password successfully captured. Cell phone number successfully added.";
    }

public boolean checkUserName(String username) {
    return username != null && username.contains("_") && username.length() <= 5;
}

public boolean checkPasswordComplexity(String password) {
    return password != null
            && password.length() >= 8
            && password.matches(".*[A-Z].*")
            && password.matches(".*[0-9].*")
            && password.matches(".*[^A-Za-z0-9].*");
}

public boolean checkCellPhoneNumber(String cell) {
    return cell != null && cell.matches("\\+27\\d{9}");
}

    boolean loginUser(String LoginUsername, String LoginPassword) {
    Object storedUsername = null;
       return storedUsername != null
               && storedUsername.equals(LoginUsername)
               && storedPassword.equals(LoginPassword);
    }


    private void UnsupportedOperationException(String not_supported_yet) {
        UnsupportedOperationException("Not supported yet."); 
    }

    String returnLoginStatus(boolean LoginSuccessful) {
        if(LoginSuccessful) {
            return " A successful login";
        } else {
            return "A failed login";
        }
        }
        
}
    
  
