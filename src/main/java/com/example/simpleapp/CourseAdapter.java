package com.example.simpleapp;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Map;

public class CourseAdapter extends BaseAdapter {

    private Context context;
    private ArrayList<Map<String, Object>> courses;

    public CourseAdapter(Context context, ArrayList<Map<String, Object>> courses) {
        this.context = context;
        this.courses = courses;
    }

    @Override
    public int getCount() {
        return courses.size();
    }

    @Override
    public Object getItem(int position) {
        return courses.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        // ViewHolder pattern for better performance
        ViewHolder viewHolder;

        if (convertView == null) {
            // Inflate the layout and create a ViewHolder instance
            convertView = LayoutInflater.from(context).inflate(R.layout.course_item, parent, false);
            viewHolder = new ViewHolder();
            viewHolder.courseNameTextView = convertView.findViewById(R.id.courseNameTextView);
            viewHolder.courseCreditsTextView = convertView.findViewById(R.id.courseCreditsTextView);
            convertView.setTag(viewHolder); // Set the ViewHolder as a tag for future reference
        } else {
            // Retrieve the ViewHolder from the tag
            viewHolder = (ViewHolder) convertView.getTag();
        }

        // Get the current course
        Map<String, Object> course = courses.get(position);

        // Safely get the course name and credits
        String courseCredits = course.containsKey("Credits") ? "Credits: " + course.get("Credits").toString() : "Credits: 0";
        String courseName = course.containsKey("Name") ? course.get("Name").toString() : "Unknown Course";

        // Set the course name and credits
        viewHolder.courseNameTextView.setText(courseName);
        viewHolder.courseCreditsTextView.setText(courseCredits);

        return convertView;
    }

    // ViewHolder class for better performance with ListView
    static class ViewHolder {
        TextView courseNameTextView;
        TextView courseCreditsTextView;
    }
}