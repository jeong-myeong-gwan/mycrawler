package mj.mycrawler.model;

import java.util.ArrayList;
import java.util.List;

public class RobotsRule {

    private final List<String> allowList = new ArrayList<>();
    private final List<String> disallowList = new ArrayList<>();

    public List<String> getAllowList() {
        return allowList;
    }

    public List<String> getDisallowList() {
        return disallowList;
    }
}
