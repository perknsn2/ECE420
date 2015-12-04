package com.example.caleb.buildingdetector;

import org.opencv.core.DMatch;
import org.opencv.core.MatOfDMatch;

import java.util.List;

/**
 * Created by Tori on 12/4/2015.
 */
public class matchHelp
{
    public static float average(MatOfDMatch matches)
    {
        float ave = 0;
        List<DMatch> matchList = matches.toList();
        for(int i = 0; i<matchList.size(); i++)
        {
            ave += matchList.get(i).distance;
        }
        ave /= matchList.size();
        return ave;
    }
}
