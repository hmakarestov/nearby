package fhict.nl.nearby;


import android.view.View;
import android.view.ViewGroup;
import android.view.ViewParent;

import androidx.test.espresso.ViewInteraction;
import androidx.test.filters.LargeTest;
import androidx.test.rule.ActivityTestRule;
import androidx.test.rule.GrantPermissionRule;
import androidx.test.runner.AndroidJUnit4;

import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.hamcrest.TypeSafeMatcher;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.action.ViewActions.closeSoftKeyboard;
import static androidx.test.espresso.action.ViewActions.replaceText;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;
import static org.hamcrest.Matchers.allOf;

@LargeTest
@RunWith(AndroidJUnit4.class)
public class RegisterActivityTest {

    @Rule
    public ActivityTestRule<MainActivity> mActivityTestRule = new ActivityTestRule<>(MainActivity.class);

    @Rule
    public GrantPermissionRule mGrantPermissionRule =
            GrantPermissionRule.grant(
                    "android.permission.ACCESS_FINE_LOCATION");

    @Test
    public void registerActivityTest() {
        try {
            Thread.sleep(3000);
        }
        catch (InterruptedException e)
        {
            e.printStackTrace();
        }

        ViewInteraction appCompatButton = onView(withId(R.id.btnLaunchRegister));
        appCompatButton.perform(click());

        try {
            Thread.sleep(3000);
        }
        catch (InterruptedException e)
        {
            e.printStackTrace();
        }

        ViewInteraction appCompatEditText = onView(withId(R.id.editTextRegisterNickname));
        appCompatEditText.perform(replaceText("testing"), closeSoftKeyboard());

        ViewInteraction appCompatEditText2 = onView(withId(R.id.editTextRegisterEmail));
        appCompatEditText2.perform(replaceText("testuser@gmail.com"), closeSoftKeyboard());

        ViewInteraction appCompatEditText3 = onView(withId(R.id.editTextRegisterPassword));
        appCompatEditText3.perform(replaceText("123456789"), closeSoftKeyboard());

        ViewInteraction appCompatEditText4 = onView(withId(R.id.editTextRegisterRepeatPassword));
        appCompatEditText4.perform(replaceText("123456789"), closeSoftKeyboard());

        try {
            Thread.sleep(5000);
        }
        catch (InterruptedException e)
        {
            e.printStackTrace();
        }

        ViewInteraction appCompatButton2 = onView(withId(R.id.btnRegisterEmail));
        appCompatButton2.perform(click());

        try {
            Thread.sleep(10000);
        }
        catch (InterruptedException e)
        {
            e.printStackTrace();
        }

        ViewInteraction textView = onView(withId(R.id.textView_user_testing));
        textView.check(matches(withText("LOGGED IN testuser@gmail.com")));
    }
}
