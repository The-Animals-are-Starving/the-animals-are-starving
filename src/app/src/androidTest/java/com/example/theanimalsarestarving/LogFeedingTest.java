package com.example.theanimalsarestarving;

import static androidx.test.espresso.Espresso.onData;
import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.Espresso.pressBack;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.action.ViewActions.closeSoftKeyboard;
import static androidx.test.espresso.action.ViewActions.typeText;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.assertThat;
import static androidx.test.espresso.matcher.ViewMatchers.hasDescendant;
import static androidx.test.espresso.matcher.ViewMatchers.hasSibling;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withClassName;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;
import static androidx.test.espresso.matcher.ViewMatchers.withParent;
import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.Matchers.hasToString;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.core.AllOf.allOf;
import static androidx.test.espresso.contrib.RecyclerViewActions.actionOnItem;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.Intent;
import android.view.View;
import android.widget.EditText;

import androidx.test.core.app.ActivityScenario;
import androidx.test.espresso.matcher.ViewMatchers;
import androidx.test.espresso.util.TreeIterables;
import androidx.test.platform.app.InstrumentationRegistry;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import org.hamcrest.Matcher;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import com.example.theanimalsarestarving.activities.MainActivity;

import java.util.Iterator;

@RunWith(AndroidJUnit4.class)
public class LogFeedingTest {

    public int getCount(final Matcher<View> matcher) {
        final int[] count = {0};

        onView(ViewMatchers.isRoot()).check((view, noViewFoundException) -> {
            if (noViewFoundException != null) throw noViewFoundException;

            Iterator<View> iterator = TreeIterables.breadthFirstViewTraversal(view).iterator();
            while (iterator.hasNext()) {
                View next = iterator.next();
                if (matcher.matches(next)) {
                    count[0]++;
                }
            }
        });

        return count[0];
    }

    @Before
    public void setUp() {
        // Get application context
        Context context = InstrumentationRegistry.getInstrumentation().getTargetContext();

        // Set SharedPreferences BEFORE launching the activity
        SharedPreferences sharedPreferences = context.getSharedPreferences("AppPrefs", Context.MODE_PRIVATE);
        sharedPreferences.edit()
                .putBoolean("isLoggedIn", true)
                .putString("userEmail", "bob@gmail.com")
                .putString("userName", "Bob")
                .apply();
    }

    @Test
    public void testLogFeedingUseCase() {
        // Launch MainActivity manually after setting up SharedPreferences
        try (ActivityScenario<MainActivity> scenario = ActivityScenario.launch(MainActivity.class)) {
            //User scrolls through the list of pets on the base page to find the pet being fed.
            onView(withId(R.id.feed_button)).check(matches(isDisplayed()));
            onView(withId(R.id.feed_button)).perform(click());
            onView(withId(R.id.title))
                    .check(matches(isDisplayed()));

            onView(allOf(withText("kitty cat"), withParent(withId(R.id.petInfoLayout))))
                    .check(matches(isDisplayed()));

            onView(allOf(withId(R.id.indicate_fed_button), hasSibling(withText("kitty cat"))))
                    .check(matches(isDisplayed()));

            onView(allOf(withText("NOT FED"), hasSibling(withText("kitty cat"))))
                    .check(matches(isDisplayed()));

            //User presses the corresponding “Feed Pet” button to confirm that the pet has been fed.
            onView(allOf(withId(R.id.indicate_fed_button), hasSibling(withText("kitty cat"))))
                    .perform(click());

            //Find the EditText and enter the value "1"
            onView(withClassName(is(EditText.class.getName())))
                    .perform(typeText("1"), closeSoftKeyboard());

            //Click the "Feed" button
            onView(withText("Feed")).perform(click());


            // User is prompted with a success message indicating that the log has been updated successfully.
            onView(allOf(withText("FED"), hasSibling(withText("kitty cat"))))
                    .check(matches(isDisplayed()));


            pressBack();

            onView(withId(R.id.feeding_history_button)).check(matches(isDisplayed()));
            onView(withId(R.id.feeding_history_button)).perform(click());
            onView(withId(R.id.title))
                    .check(matches(isDisplayed()));

            onView(withText("Pet Name")).check(matches(isDisplayed()));
            onView(withText("Fed By")).check(matches(isDisplayed()));
            onView(withText("Time")).check(matches(isDisplayed()));
            assertThat("Bob did not appear", getCount(withText("Bob")), greaterThan(0));
        }
    }
}