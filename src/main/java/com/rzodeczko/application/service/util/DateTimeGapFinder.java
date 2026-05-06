package com.rzodeczko.application.service.util;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.joda.time.DateTime;
import org.joda.time.Interval;

import java.util.ArrayList;
import java.util.List;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class DateTimeGapFinder {

    public static List<Interval> findGaps(List<Interval> existingIntervals, Interval searchInterval) {
        List<Interval> gaps = new ArrayList<>();

        DateTime searchStart = searchInterval.getStart();
        DateTime searchEnd = searchInterval.getEnd();

        if (hasNoOverlap(existingIntervals, searchInterval, searchStart, searchEnd)) {
            gaps.add(searchInterval);
            return gaps;
        }

        // create a sub-list that excludes interval which does not overlap with
        // searchInterval
        List<Interval> subExistingList = removeNoneOverlappingIntervals(existingIntervals, searchInterval);
        DateTime subEarliestStart = subExistingList.get(0).getStart();
        DateTime subLatestStop = subExistingList.get(subExistingList.size() - 1).getEnd();

        // in case the searchInterval is wider than the union of the existing
        // include searchInterval.start => earliestExisting.start
        if (searchStart.isBefore(subEarliestStart)) {
            gaps.add(new Interval(searchStart, subEarliestStart));
        }

        // get all the gaps in the existing list
        gaps.addAll(getExistingIntervalGaps(subExistingList));

        // include latestExisting.stop => searchInterval.stop
        if (searchEnd.isAfter(subLatestStop)) {
            gaps.add(new Interval(subLatestStop, searchEnd));
        }
        return gaps;
    }

    private static List<Interval> getExistingIntervalGaps(List<Interval> existingList) {
        List<Interval> gaps = new ArrayList<>();
        Interval current = existingList.get(0);
        for (int i = 1; i < existingList.size(); i++) {
            Interval next = existingList.get(i);
            Interval gap = current.gap(next);
            if (gap != null)
                gaps.add(gap);
            current = next;
        }
        return gaps;
    }

    private static List<Interval> removeNoneOverlappingIntervals(List<Interval> existingIntervals, Interval searchInterval) {
        List<Interval> subExistingList = new ArrayList<>();
        for (Interval interval : existingIntervals) {
            if (interval.overlaps(searchInterval)) {
                subExistingList.add(interval);
            }
        }
        return subExistingList;
    }

    private static boolean hasNoOverlap(List<Interval> existingIntervals, Interval searchInterval, DateTime searchStart, DateTime searchEnd) {
        DateTime earliestStart = existingIntervals.get(0).getStart();
        DateTime latestStop = existingIntervals.get(existingIntervals.size() - 1).getEnd();
        // return the entire search interval if it does not overlap with
        // existing at all
        return searchEnd.isBefore(earliestStart) || searchStart.isAfter(latestStop);
    }
}
