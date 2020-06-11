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

import java.util.concurrent.Delayed;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.action.ViewActions.closeSoftKeyboard;
import static androidx.test.espresso.action.ViewActions.longClick;
import static androidx.test.espresso.action.ViewActions.replaceText;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withClassName;
import static androidx.test.espresso.matcher.ViewMatchers.withContentDescription;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.is;

@LargeTest
@RunWith(AndroidJUnit4.class)
public class LoginAndAddFriendTest {

    @Rule
    public ActivityTestRule<MainActivity> mActivityTestRule = new ActivityTestRule<>(MainActivity.class);

    @Rule
    public GrantPermissionRule mGrantPermissionRule =
            GrantPermissionRule.grant(
                    "android.permission.ACCESS_FINE_LOCATION");

    @Test
    public void loginAndAddFriendTest() throws InterruptedException {
        ViewInteraction appCompatEditText = onView((withId(R.id.editTextLoginEmail)));
        appCompatEditText.perform(replaceText("dceltare99@gmail.com"), closeSoftKeyboard());

        Thread.sleep(100);

        ViewInteraction appCompatEditText2 = onView(withId(R.id.editTextLoginPassword));
        appCompatEditText2.perform(click());

        Thread.sleep(100);

        ViewInteraction appCompatEditText5 = onView(withId(R.id.editTextLoginPassword));
        appCompatEditText5.perform(replaceText("parola1234"), closeSoftKeyboard());

        Thread.sleep(100);

        ViewInteraction appCompatButton = onView(withId(R.id.btnLoginEmail));
        appCompatButton.perform(click());

        Thread.sleep(1000);

        ViewInteraction appCompatImageButton = onView(
                allOf(withContentDescription("Open navigation drawer"),
                        childAtPosition(
                                allOf(withId(R.id.toolbar),
                                        childAtPosition(
                                                withClassName(is("com.google.android.material.appbar.AppBarLayout")),
                                                0)),
                                1),
                        isDisplayed()));
        appCompatImageButton.perform(click());

        Thread.sleep(100);

        ViewInteraction OpenFriends = onView(allOf(childAtPosition(
                allOf(withId(R.id.design_navigation_view),
                        childAtPosition(
                                withId(R.id.nav_view),
                                0)),
                3),
                isDisplayed()));
        OpenFriends.perform(click());

        Thread.sleep(100);

        ViewInteraction appCompatEditText6 = onView(withId(R.id.editText_add_friend));
        appCompatEditText6.perform(click());

        Thread.sleep(100);

        ViewInteraction appCompatEditText7 = onView(withId(R.id.editText_add_friend));
        appCompatEditText7.perform(replaceText("Dasdade"), closeSoftKeyboard());

        Thread.sleep(100);

        ViewInteraction appCompatButton2 = onView(withId(R.id.button_add_friend));
        appCompatButton2.perform(click());
    }

    private static Matcher<View> childAtPosition(
            final Matcher<View> parentMatcher, final int position) {

        return new TypeSafeMatcher<View>() {
            @Override
            public void describeTo(Description description) {
                description.appendText("Child at position " + position + " in parent ");
                parentMatcher.describeTo(description);
            }

            @Override
            public boolean matchesSafely(View view) {
                ViewParent parent = view.getParent();
                return parent instanceof ViewGroup && parentMatcher.matches(parent)
                        && view.equals(((ViewGroup) parent).getChildAt(position));
            }
        };
    }
}
