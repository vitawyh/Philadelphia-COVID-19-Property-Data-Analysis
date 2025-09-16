package dataanalysis.datamanagement;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class CSVFileReader {

    private final CharacterReader reader;

    public CSVFileReader(CharacterReader reader) {
        this.reader = reader;
    }

    public String[] readRow() throws IOException {
        StringBuilder resultString = new StringBuilder();
        List<String> result = new ArrayList<>();

        int ch;
        int line = 1, column = 0, row = 0, field = 0;

        State state = State.START_FIELD;
        boolean isCR = false;

        while ((ch = reader.read()) != -1) {
            char c = (char) ch;
            column++;

            if (isCR) {
                if (state != State.IN_QUOTED_FIELD) {
                    if (c == '\n') {
                        result.add(resultString.toString());
                        return result.toArray(new String[0]);
                    }
                } else {
                    resultString.append('\r');
                    isCR = false;
                }
            }

            switch (state) {
                case START_FIELD:
                    switch (c) {
                        case ',':
                            result.add("");
                            field++;
                            break;
                        case '"':
                            state = State.IN_QUOTED_FIELD;
                            break;
                        case '\r':
                            isCR = true;
                            break;
                        case '\n':
                            result.add("");
                            return result.toArray(new String[0]);
                        default:
                            resultString.append(c);
                            state = State.IN_FIELD;
                            break;
                    }
                    break;

                case IN_FIELD:
                    switch (c) {
                        case ',':
                            result.add(resultString.toString());
                            resultString.setLength(0);
                            field++;
                            state = State.START_FIELD;
                            break;
                        case '"':
                            throw new IOException(errorMessage("Unexpected quote", line, column, row, field));
                        case '\r':
                            isCR = true;
                            break;
                        case '\n':
                            result.add(resultString.toString());
                            return result.toArray(new String[0]);
                        default:
                            resultString.append(c);
                            break;
                    }
                    break;

                case IN_QUOTED_FIELD:
                    switch (c) {
                        case '"':
                            state = State.AFTER_QUOTE;
                            break;
                        default:
                            resultString.append(c);
                            break;
                    }
                    break;

                case AFTER_QUOTE:
                    switch (c) {
                        case '"':
                            resultString.append('"');
                            state = State.IN_QUOTED_FIELD;
                            break;
                        case ',':
                            result.add(resultString.toString());
                            resultString.setLength(0);
                            field++;
                            state = State.START_FIELD;
                            break;
                        case '\r':
                            isCR = true;
                            break;
                        case '\n':
                            result.add(resultString.toString());
                            row++;
                            field = 0;
                            return result.toArray(new String[0]);
                        default:
                            throw new IOException(errorMessage("Unexpected character after closing quote", line, column, row, field));
                    }
                    break;
            }
        }

        if (isCR) {
            throw new IOException(errorMessage("File ends with CR not followed by LF", line, column, row, field));
        }

        if (state == State.IN_QUOTED_FIELD) {
            throw new IOException(errorMessage("Unclosed quoted field", line, column, row, field));
        }

        if (state == State.IN_FIELD || state == State.AFTER_QUOTE || resultString.length() > 0 || !result.isEmpty()) {
            result.add(resultString.toString());
            row++;
            field = 0;
            return result.toArray(new String[0]);
        }

        return null;
    }

    private static String errorMessage(String message, int line, int column, int row, int field) {
        return String.format("%s at line %d, column %d, row %d, field %d", message, line, column, row, field);
    }

    private enum State {
        START_FIELD, IN_FIELD, IN_QUOTED_FIELD, AFTER_QUOTE
    }
}
