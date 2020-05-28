package fhict.nl.nearby;
import android.widget.EditText;
import org.junit.Test;
import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockingDetails;

public class RegisterFormValidationUnitTest {

    EditText etEmail = mock(EditText.class);
    EditText etPassword = mock(EditText.class);
    EditText etRepeatPassword = mock(EditText.class);

    private boolean ValidForm(){
        String emailPattern = "[a-zA-Z0-9._-]+@[a-z]+\\.+[a-z]+";
        String email = "test@email.com";
        String password = "password1234";
        String repeatPassword = "password1234";

        if(email.isEmpty() || password.isEmpty() || repeatPassword.isEmpty()){
            if(email.isEmpty()){
                etEmail.setError("Required");
            }

            if(password.isEmpty()){
                etPassword.setError("Required");
            }

            if(repeatPassword.isEmpty()){
                etRepeatPassword.setError("Required");
            }
            return false;
        }

        if(!email.matches(emailPattern)){
            etEmail.setError("Email does not contain a valid format.");
            return false;
        }

        if(!password.equals(repeatPassword)){
            etRepeatPassword.setError("Please enter matching password");
            return false;
        }

        if(password.length() < 8){
            etPassword.setError("Password should be 8 characters or more.");
            return false;
        }

        return true;
    }
    @Test
    public void TestFormValidation()
    {
        assertEquals(true, ValidForm());
    }
}
