package ir.notopia.android.database.entity;

import android.os.Parcel;
import android.os.Parcelable;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.Ignore;
import androidx.room.PrimaryKey;

@Entity(tableName = "scans")
public class ScanEntity implements Parcelable {
    @PrimaryKey(autoGenerate = true)
    @NonNull
    @ColumnInfo(name = "scan_id")
    private int id;
    private String image;
    private String category;
    private String year;
    private String month;
    private String day;
    private String qrCode;
    private boolean needEdit;

    public ScanEntity() {

    }

    @Ignore
    public ScanEntity(String image, String category, String year, String month, String day, String qrCode, boolean needEdit) {
        this.image = image;
        this.category = category;
        this.year = year;
        this.month = month;
        this.day = day;
        this.qrCode = qrCode;
        this.needEdit = needEdit;
    }

    protected ScanEntity(Parcel in) {
        id = in.readInt();
        image = in.readString();
        category = in.readString();
        year = in.readString();
        month = in.readString();
        day = in.readString();
        qrCode = in.readString();
        needEdit = in.readByte() != 0;
    }

    public static final Creator<ScanEntity> CREATOR = new Creator<ScanEntity>() {
        @Override
        public ScanEntity createFromParcel(Parcel in) {
            return new ScanEntity(in);
        }

        @Override
        public ScanEntity[] newArray(int size) {
            return new ScanEntity[size];
        }
    };

    public boolean isNeedEdit() {
        return needEdit;
    }

    public void setNeedEdit(boolean needEdit) {
        this.needEdit = needEdit;
    }

    public String getYear() {
        return year;
    }

    public void setYear(String year) {
        this.year = year;
    }

    @Override
    public String toString() {
        return "ScanEntity{" +
                "id=" + id +
                ", image='" + image + '\'' +
                ", category='" + category + '\'' +
                ", year='" + year + '\'' +
                ", month='" + month + '\'' +
                ", day='" + day + '\'' +
                ", qrCode='" + qrCode + '\'' +
                ", needEdit=" + needEdit +
                '}';
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }

    public String getMonth() {
        return month;
    }

    public void setMonth(String month) {
        this.month = month;
    }

    public String getDay() {
        return day;
    }

    public void setDay(String day) {
        this.day = day;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getQrCode() {
        return qrCode;
    }

    public void setQrCode(String qrCode) {
        this.qrCode = qrCode;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(id);
        dest.writeString(image);
        dest.writeString(category);
        dest.writeString(year);
        dest.writeString(month);
        dest.writeString(day);
        dest.writeString(qrCode);
        dest.writeByte((byte) (needEdit ? 1 : 0));
    }
}
