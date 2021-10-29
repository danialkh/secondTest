package ir.notopia.android.database;

import android.content.Context;

import java.io.File;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import ir.notopia.android.database.entity.ArEntity;
import ir.notopia.android.database.entity.CategoryEntity;
import ir.notopia.android.database.entity.NoteEntity;
import ir.notopia.android.database.entity.NotificationResponse;
import ir.notopia.android.database.entity.ScanEntity;

public class AppRepository {
    private static AppRepository ourInstance;
    //    public LiveData<List<NotificationResponse>> mNotifications;
    public List<NotificationResponse> mNotifications;
    public List<NoteEntity> mNotes;
    public List<ArEntity> mArs;


    private AppDatabase mDb;
    private Executor executor = Executors.newSingleThreadExecutor();

    private AppRepository(Context context) {
        mDb = AppDatabase.getInstance(context);
//        mNotifications = getResponses();

    }

    public static AppRepository getInstance(Context context) {
        if (ourInstance == null) {
            ourInstance = new AppRepository(context);
        }

        return ourInstance;
    }

    private static Date getDate(int diff) {
        GregorianCalendar cal = new GregorianCalendar();
        cal.add(Calendar.MILLISECOND, diff);
        return cal.getTime();
    }

    public static List<NotificationResponse> getResponses() {
        List<NotificationResponse> notes = new ArrayList<>();
        NotificationResponse mNotifications = new NotificationResponse("5c066535f0b56b7d813eea61", true, "تست شماره یک", "5c0615cdf0b56b7d813eea6c", "برای با دوم اطلاع رسانی را تست می کنیم.", getDate(0), "5c02afd3f68e3b4980108518", getDate(1), getDate(2), 0);
        NotificationResponse mNotifications1 = new NotificationResponse("5c066535f0b56b7d813eea62", true, "تست شماره دو", "5c0615cdf0b56b7d813eea6c", "برای با دوم اطلاع رسانی را تست می کنیم.", getDate(3), "5c02afd3f68e3b4980108518", getDate(4), getDate(5), 0);
        NotificationResponse mNotifications2 = new NotificationResponse("5c066535f0b56b7d813eea63", true, "تست شماره سه", "5c0615cdf0b56b7d813eea6c", "برای با دوم اطلاع رسانی را تست می کنیم.", getDate(6), "5c02afd3f68e3b4980108518", getDate(7), getDate(8), 0);
        notes.add(mNotifications);
        notes.add(mNotifications1);
        notes.add(mNotifications2);

        return notes;
    }

    public boolean addNotification(final NotificationResponse notificationResponse) {
        NotificationResponse oldNotificationResponse = mDb.responseDao().getResponseById(notificationResponse.getId());
        if (oldNotificationResponse == null || oldNotificationResponse.getUpdatedAt().compareTo(notificationResponse.getUpdatedAt()) == 1) {
            executor.execute(new Runnable() {
                @Override
                public void run() {
                    mDb.responseDao().insertResponse(notificationResponse);
                }
            });
            return true;
        }
        return false;
    }

    public NotificationResponse getNotification(String id) {
        return mDb.responseDao().getResponseById(id);
    }

    public List<NotificationResponse> getAllNotificatios() {
        return mDb.responseDao().getAll();
    }

    public List<NoteEntity> getAllNotes() {
        return mDb.noteDao().getAll();

    }

    public void deleteAllNotes() {
        executor.execute(() -> mDb.noteDao().deleteAll());
    }

    public NoteEntity getNoteById(int noteId) {
        return mDb.noteDao().getNoteById(noteId);
    }

    public List<NoteEntity> getDayNotes(String dayId) {
        return mDb.noteDao().getNotesByDayId(dayId);
    }

    public void insertNote(final NoteEntity note) {
        executor.execute(() -> mDb.noteDao().insertNote(note));
    }

    public void deleteNote(final NoteEntity note) {
        executor.execute(() -> mDb.noteDao().deleteNote(note));
    }

    public void insertScan(final ScanEntity scan) {
        executor.execute(() -> mDb.scanDao().insertAll(scan));
    }

    public void deleteScan(final ScanEntity scan) {
        executor.execute(() -> mDb.scanDao().delete(scan));

    }
    public void deleteScanByImage(String filePath) {
        executor.execute(() -> mDb.scanDao().deleteByImage(filePath));
    }

    public ScanEntity getScan(final int scanId) {
        return mDb.scanDao().findById(scanId);

    }
    public ScanEntity getScanByImage(final String scanImage) {
        return mDb.scanDao().findByImage(scanImage);

    }

    public void updateScan(final ScanEntity scan) {
        executor.execute(() -> mDb.scanDao().updateScan(scan));
    }

    public List<ScanEntity> getScans() {
        return mDb.scanDao().getAll();
    }


    // Ar database functions
    public void insertAr(final ArEntity ar) {
        executor.execute(() -> mDb.arDao().insertAr(ar));
    }
    public List<ArEntity> getArs() {
        return mDb.arDao().getAll();
    }
    public void deleteAllArs() {
        executor.execute(() -> mDb.arDao().deleteAll());
    }


    // Category database functions
    public List<CategoryEntity> getCategorys() {
        return mDb.CategoryDao().getAll();
    }

    public CategoryEntity getCategoryById(int ctgId) {
        return mDb.CategoryDao().getCategoryById(ctgId);
    }


    public void insertCategory(final CategoryEntity category) {
        executor.execute(() -> mDb.CategoryDao().insertCategory(category));
    }
    public void updateCategory(final CategoryEntity category) {
        executor.execute(() -> mDb.CategoryDao().updateCategory(category));
    }


}
