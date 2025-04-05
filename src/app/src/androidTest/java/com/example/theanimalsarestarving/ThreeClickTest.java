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
public class ThreeClickTest {

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
    public void threeClickTest() {
        try (ActivityScenario<MainActivity> scenario = ActivityScenario.launch(MainActivity.class)) {

            //feeding
            onView(withId(R.id.feed_button)).perform(click()); //click 1
            onView(allOf(withId(R.id.indicate_fed_button), hasSibling(withText("puppy dog")))).
                    perform(click()); //click 2

            //Find the EditText and enter the value "1"
            onView(withClassName(is(EditText.class.getName())))
                    .perform(typeText("1"), closeSoftKeyboard());

            //Click the "Feed" button
            onView(withText("Feed")).perform(click()); //click 3


            //check
            pressBack();
            onView(withId(R.id.feed_button)).perform(click());
            onView(allOf(withText("FED"), hasSibling(withText("puppy dog"))))
                    .check(matches(isDisplayed())); //make sure pet is fed



            //history
            pressBack();
            onView(withId(R.id.feeding_history_button)).perform(click());  //click 1

            //check
            onView(withId(R.id.title))
                    .check(matches(isDisplayed())); //make sure the logs page opens
        }
    }
}