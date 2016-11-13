package ru.spbau.savethemoment.momentmanager;

import android.os.AsyncTask;

import java.util.LinkedList;
import java.util.List;

public class LoadMomentsTask extends AsyncTask<List<String>, Void, List<Moment>> {
    @Override
    protected List<Moment> doInBackground(List<String>... params) {
        List<String> tags = new LinkedList<>();
        for (List<String> tagsList : params) {
            tags.addAll(tagsList);
        }
        if (tags.isEmpty()) {
            return MomentManager.getMoments();
        } else {
            return MomentManager.getMomentsByTags(tags);
        }
    }
}
