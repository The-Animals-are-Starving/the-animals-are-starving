package com.example.theanimalsarestarving;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.Espresso.pressBack;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.hasDescendant;
import static androidx.test.espresso.matcher.ViewMatchers.hasSibling;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;
import static androidx.test.espresso.matcher.ViewMatchers.withParent;
import static org.hamcrest.core.AllOf.allOf;
import static androidx.test.espresso.contrib.RecyclerViewActions.actionOnItem;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.Intent;

import androidx.test.core.app.ActivityScenario;
import androidx.test.platform.app.InstrumentationRegistry;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import com.example.theanimalsarestarving.activities.MainActivity;

@RunWith(AndroidJUnit4.class)
public class EspressoTest {

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

            // User is prompted with a success message indicating that the log has been updated successfully.
            onView(allOf(withText("FED"), hasSibling(withText("kitty cat"))))
                    .check(matches(isDisplayed()));

            // System updates the feeding log with the pet's ID, user ID, date, and amount of food.
            pressBack();

            onView(withId(R.id.feeding_history_button)).check(matches(isDisplayed()));
            onView(withId(R.id.feeding_history_button)).perform(click());
            onView(withId(R.id.title))
                    .check(matches(isDisplayed()));

            onView(withText("Pet Name")).check(matches(isDisplayed()));
            onView(withText("Fed By")).check(matches(isDisplayed()));
            onView(withText("Time")).check(matches(isDisplayed()));
            onView(withText("kitty cat")).check(matches(isDisplayed()));
            onView(withText("Bob")).check(matches(isDisplayed()));
        }
    }

    @Test
    public void testNotifications() {
        // Launch MainActivity manually after setting up SharedPreferences
        try (ActivityScenario<MainActivity> scenario = ActivityScenario.launch(MainActivity.class)) {
            onView(withId(R.id.notify_button)).check(matches(isDisplayed()));
            onView(withId(R.id.notify_button)).perform(click());
            //after clicking, make sure the popup opens
            onView(withText("Bob")).check(matches(isDisplayed()));

            onView(allOf(withText("Notify"), hasSibling(withText("Bob"))))
                    .perform(click());
        }
    }
}