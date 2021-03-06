package ru.spbau.savethemoment.datamanagers;

import android.content.Context;
import android.database.Cursor;
import android.location.Location;

import com.google.common.collect.ImmutableSet;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.annotation.Config;

import java.util.Calendar;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

import ru.spbau.savethemoment.BuildConfig;
import ru.spbau.savethemoment.common.Moment;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

@Config(constants = BuildConfig.class, sdk = 21, manifest = "/src/main/AndroidManifest.xml")
@RunWith(RobolectricTestRunner.class)
public class MomentManagerTest {

    private MomentManager momentManager;

    private Moment moment;
    private Moment moment2;
    private UUID id;
    private UUID id2;

    @Before
    public void setUp() {
        Context context = RuntimeEnvironment.application;
        momentManager = new MomentManager(context);
        id = UUID.randomUUID();
        id2 = UUID.randomUUID();
    }

    @Test
    public void insertMomentTest() {
        moment = new Moment(id, "Title", "Description", Calendar.getInstance(),
                new Location(""), "Address", Collections.<String>emptySet());
        momentManager.insertMoment(moment);
    }

    @Test
    public void getMomentByIdTest() {
        moment = new Moment(id, "Title", "Description", Calendar.getInstance(),
                new Location(""), "Address", Collections.<String>emptySet());
        momentManager.insertMoment(moment);
        Moment result = momentManager.getMomentById(id);
        assertNotNull(result);
        assertEquals("Title", result.getTitle());
        result = momentManager.getMomentById(id2);
        assertNull(result);
    }

    @Test
    public void getMomentsEmptyTest() {
        Cursor cursor = momentManager.getMoments(false);
        assertTrue(cursor.isAfterLast());
    }

    @Test
    public void getMomentsTest() {
        moment = new Moment(id, "Title", "Description", Calendar.getInstance(),
                new Location(""), "Address", Collections.<String>emptySet());
        momentManager.insertMoment(moment);
        Cursor cursor = momentManager.getMoments(false);
        assertTrue(cursor.moveToFirst());
        assertEquals(moment.getId().toString(), cursor.getString(cursor.getColumnIndex(MomentManager.MOMENT_ID)));
        assertEquals(7, cursor.getColumnCount());
        assertFalse(cursor.moveToNext());
    }

    @Test
    public void deleteMomentTest() {
        moment = new Moment(id, "Title", "Description", Calendar.getInstance(),
                new Location(""), "Address", Collections.<String>emptySet());
        momentManager.insertMoment(moment);
        momentManager.deleteMomentById(moment.getId());
        Cursor cursor = momentManager.getMoments(false);
        assertFalse(cursor.moveToNext());
    }

    @Test
    public void getTagsByMomentIdTest() {
        moment = new Moment(id, "Title", "Description", Calendar.getInstance(),
                new Location(""), "Address", ImmutableSet.of("testTag", "anotherTestTag"));
        momentManager.insertMoment(moment);
        Set<String> resultTags = momentManager.getTagsByMomentId(moment.getId());
        assertEquals(moment.getTags(), resultTags);
    }

    @Test
    public void getMomentsByTagsTest() {
        moment = new Moment(id, "Title", "Description", Calendar.getInstance(),
                new Location(""), "Address", ImmutableSet.of("testTag", "anotherTestTag"));
        momentManager.insertMoment(moment);
        moment2 = new Moment(id2, "Title2", "Description2", Calendar.getInstance(),
                new Location(""), "Address2", ImmutableSet.of("testTag"));
        momentManager.insertMoment(moment2);

        Set<String> tags1 = ImmutableSet.of("anotherTestTag");
        Cursor cursor = momentManager.getMomentsByTags(tags1, false);
        assertTrue(cursor.moveToNext());
        assertEquals(moment.getId().toString(), cursor.getString(cursor.getColumnIndex(MomentManager.MOMENT_ID)));
        assertFalse(cursor.moveToNext());

        Set<String> tags2 = ImmutableSet.of("testTag");
        cursor = momentManager.getMomentsByTags(tags2, false);
        Set<String> ids = new HashSet<>();
        assertTrue(cursor.moveToNext());
        ids.add(cursor.getString(cursor.getColumnIndex(MomentManager.MOMENT_ID)));
        assertTrue(cursor.moveToNext());
        ids.add(cursor.getString(cursor.getColumnIndex(MomentManager.MOMENT_ID)));
        assertFalse(cursor.moveToNext());
        assertEquals(ImmutableSet.of(id.toString(), id2.toString()), ids);
    }

    @Test
    public void updateMomentTest() {
        moment = new Moment(id, "Title", "Description", Calendar.getInstance(),
                new Location(""), "Address", ImmutableSet.of("testTag", "anotherTestTag"));
        momentManager.insertMoment(moment);
        moment2 = new Moment(id, "Title2", "Description2", Calendar.getInstance(),
                new Location(""), "Address2", ImmutableSet.of("testTag"));
        momentManager.updateMoment(moment2);
        moment = momentManager.getMomentById(id);
        assertEquals(moment2.getTitle(), moment.getTitle());
        assertEquals(moment2.getTags(), moment.getTags());
    }
}