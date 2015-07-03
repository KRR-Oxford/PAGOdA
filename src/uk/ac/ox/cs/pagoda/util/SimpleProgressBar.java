package uk.ac.ox.cs.pagoda.util;

import uk.ac.ox.cs.pagoda.util.disposable.Disposable;

public class SimpleProgressBar extends Disposable {

    private final String name;
    private int lastPercent;
    private int maxValue;

    public SimpleProgressBar() {
        this("");
    }

    public SimpleProgressBar(String name) {
        this(name, 100);
    }

    public SimpleProgressBar(String name, int maxValue) {
        this.name = name;
        this.maxValue = maxValue;
    }

    public void update(int value) {
        int percent = value * 100 / maxValue;
        StringBuilder template = new StringBuilder("\r" + name + " [");
        for (int i = 0; i < 50; i++) {
            if (i < percent * .5) {
                template.append("=");
            } else if (i == percent * .5) {
                template.append(">");
            } else {
                template.append(" ");
            }
        }
        template.append("] %s   ");
        System.out.printf(template.toString(), percent + "%");
        System.out.flush();
        lastPercent = percent;
    }

    @Override
    public void dispose() {
        super.dispose();

        System.out.println();
    }
}
