package dataanalysis.datamanagement;

import dataanalysis.util.CovidRecord;
import java.util.List;

public interface Reader {

    List<CovidRecord> getCovidData();
}
