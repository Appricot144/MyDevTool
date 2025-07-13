import java.time.LocalDate;
import java.util.List;

public class Memo {
    private List<String> lines;
    private LocalDate date;

    public Memo(List<String> lines, LocalDate date) {
        this.lines = lines;
        this.date = date;
    }

    // getter
    public List<String> getLines() {
        return this.lines;
    }

    public LocalDate getDate() {
        return this.date;
    }
}
