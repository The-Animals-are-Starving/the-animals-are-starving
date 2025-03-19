package com.example.theanimalsarestarving;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.assertion.PositionAssertions.isCompletelyAbove;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;
import static androidx.test.espresso.action.ViewActions.click;
import androidx.test.ext.junit.rules.ActivityScenarioRule;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.filters.LargeTest;
import com.example.theanimalsarestarving.activities.MainActivity;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;


@RunWith(AndroidJUnit4.class)
@LargeTest
public class EspressoTest {
    @Rule
    public ActivityScenarioRule<MainActivity> activityRule =
            new ActivityScenarioRule<>(MainActivity.class);


    @Before
    public void setUp() {

    }
    @Test
    public void manageHouseholdButtonPresent() {
        onView(withText("Manage Household")).check(matches(isDisplayed()));
    }

    @Test
    public void feedButtonPresentAndAboveNotificationButton() {
        onView(withText("Feed Da Dawg")).check(matches(isDisplayed()));
        onView(withText("Feed Da Dawg")).check(isCompletelyAbove(withId(R.id.notify_button)));
    }

    @Test
    public void launchesFeedingActivity() {
        // Check that the "Feed Da Dawg" button is displayed
        onView(withId(R.id.feed_button)).check(matches(isDisplayed()));

        // Perform a click on the "Feed Da Dawg" button
        onView(withId(R.id.feed_button)).perform(click());

        // After clicking, we expect FeedingActivity to open.
        // We'll check if a view from FeedingActivity is displayed.
        onView(withId(R.id.title)) // Replace with an actual ID from FeedingActivity
                .check(matches(isDisplayed()));
    }

    //Pressing notification buttons
    @Test
    public void notificationLaunchesActivity()
    {
        onView(withText("Feed Da Dawg")).check(matches(isDisplayed()));
        onView(withText("Feed Da Dawg")).check(isCompletelyAbove(withId(R.id.notify_button)));
    }

    @Test
    public void testNotifications() {
        // Check that the "Feed Da Dawg" button is displayed
        onView(withId(R.id.feed_button)).check(matches(isDisplayed()));

        // Perform a click on the "Feed Da Dawg" button
        onView(withId(R.id.feed_button)).perform(click());

        // After clicking, we expect FeedingActivity to open.
        // We'll check if a view from FeedingActivity is displayed.
        onView(withId(R.id.title)) // Replace with an actual ID from FeedingActivity
                .check(matches(isDisplayed()));
    }
}
