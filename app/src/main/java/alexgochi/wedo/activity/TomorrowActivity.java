package alexgochi.wedo.activity;

import android.content.ContentValues;
import android.content.DialogInterface;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.baoyz.swipemenulistview.SwipeMenu;
import com.baoyz.swipemenulistview.SwipeMenuCreator;
import com.baoyz.swipemenulistview.SwipeMenuItem;
import com.baoyz.swipemenulistview.SwipeMenuListView;

import java.util.ArrayList;

import alexgochi.wedo.R;
import alexgochi.wedo.TaskContract;
import alexgochi.wedo.TaskDBHelper;

public class TomorrowActivity extends AppCompatActivity {
    private TaskDBHelper mHelper;
    private SwipeMenuListView LTomorrow;
    private ArrayAdapter<String> mAdapter;
    ImageView imageTomorrow;
    TextView tomorrow;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tomorrow);

        mHelper = new TaskDBHelper(this);

        imageTomorrow = (ImageView) findViewById(R.id.tomorrow);
        imageTomorrow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                add_list();
            }
        });

        LTomorrow = (SwipeMenuListView) findViewById(R.id.list_tomorrow);
        updateUI();

        tomorrow = (TextView) findViewById(R.id.total_tomorrow);
        String totalList = getString(R.string.total);
        totalList = String.format(totalList, LTomorrow.getAdapter().getCount());
        tomorrow.setText(totalList);

        SwipeMenuCreator creator = new SwipeMenuCreator() {
            @Override
            public void create(SwipeMenu menu) {
                // create "delete" item
                SwipeMenuItem deleteItem = new SwipeMenuItem(getApplicationContext());
                // set item background
                deleteItem.setBackground(new ColorDrawable(Color.rgb(0xF9, 0x3F, 0x25)));
                // set item width
                deleteItem.setWidth(70);
                // set a icon
                deleteItem.setIcon(R.drawable.ic_delete);
                // add to menu
                menu.addMenuItem(deleteItem);
            }
        };
        // set creator
        LTomorrow.setMenuCreator(creator);

        LTomorrow.setOnMenuItemClickListener(new SwipeMenuListView.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(int position, SwipeMenu menu, int index) {
                switch (index) {
                    case 0:
                        deleteTask();
                        Toast.makeText(getApplicationContext(), "List Deleted", Toast.LENGTH_SHORT).show();
                        break;
                }
                // false : close the menu; true : not close the menu
                return false;
            }
        });
    }

    private void updateUI() {
        ArrayList<String> taskList = new ArrayList<>();
        SQLiteDatabase db = mHelper.getReadableDatabase();
        Cursor cursor = db.query(TaskContract.TaskEntry.TABLE,
                new String[]{TaskContract.TaskEntry._ID, TaskContract.TaskEntry.COL_TASK_TITLE},
                null, null, null, null, null);

        while (cursor.moveToNext()) {
            int idx = cursor.getColumnIndex(TaskContract.TaskEntry.COL_TASK_TITLE);
            taskList.add(cursor.getString(idx));
        }

        if (mAdapter == null) {
            mAdapter = new ArrayAdapter<>(this, R.layout.add_todo, R.id.task_title, taskList);
            LTomorrow.setAdapter(mAdapter);

        } else {
            mAdapter.clear();
            mAdapter.addAll(taskList);
            mAdapter.notifyDataSetChanged();
        }

        cursor.close();
        db.close();
    }

    public void add_list() {
        LayoutInflater inflater = TomorrowActivity.this.getLayoutInflater();
        final View dialogLayout = inflater.inflate(R.layout.add_activity, null);
        AlertDialog dialog = new AlertDialog.Builder(this)
                .setView(dialogLayout)
                .setPositiveButton("Add", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        EditText mNote = dialogLayout.findViewById(R.id.note);
                        if (mNote.getText().toString().equals("")) {
                            showToast();
                        } else {
                            String mNoteTodo = mNote.getText().toString();
                            SQLiteDatabase db = mHelper.getWritableDatabase();
                            ContentValues values = new ContentValues();
                            values.put(TaskContract.TaskEntry.COL_TASK_TITLE, mNoteTodo);
                            db.insertWithOnConflict(TaskContract.TaskEntry.TABLE,
                                    null, values, SQLiteDatabase.CONFLICT_REPLACE);
                            db.close();
                            updateUI();
                        }
                    }
                })
                .setNegativeButton("Cancel", null)
                .create();
        dialog.show();
    }

    private void showToast() {
        Toast toast = Toast.makeText(getApplicationContext(), R.string.input_todo, Toast.LENGTH_SHORT);
        toast.show();
    }

    public void deleteTask() {
        TextView taskTextView = (TextView) findViewById(R.id.task_title);
        String task = String.valueOf(taskTextView.getText());
        SQLiteDatabase db = mHelper.getWritableDatabase();
        db.delete(TaskContract.TaskEntry.TABLE,
                TaskContract.TaskEntry.COL_TASK_TITLE+ " = ?",
                new String[]{task});
        db.close();
        updateUI();
    }
}
