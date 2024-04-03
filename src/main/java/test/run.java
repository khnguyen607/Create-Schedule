package test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

class run {

    private static ArrayList<List<NormalizedCourse>> listSchedule = new ArrayList<>();

    // Class đại diện cho môn học
    static class Course {

        String name;
        int dayOfWeek; // Thứ trong tuần (1-7)
        int shift;

        Course(String name, int dayOfWeek, int shift) {
            this.name = name;
            this.dayOfWeek = dayOfWeek;
            this.shift = shift;
        }
    }

    // Class đại diện cho môn học sau khi chuẩn hóa
    static class NormalizedCourse {

        String name;
        int shift;

        NormalizedCourse(String name, int shift) {
            this.name = name;
            this.shift = shift;
        }
    }

    // Hàm chuẩn hóa danh sách môn học
    static List<NormalizedCourse> normalize(List<Course> courses) {
        List<NormalizedCourse> normalizedCourses = new ArrayList<>();
        for (Course course : courses) {
            normalizedCourses.add(new NormalizedCourse(course.name, 4 * (course.dayOfWeek - 1) + course.shift));
        }
        return normalizedCourses;
    }

    // Hàm in ra lịch học
    static void printSchedule(List<NormalizedCourse> currentSchedule) {
//        String[][] arrSchedule = new String[5][7];
//        arrSchedule[0] = new String[]{"Ca/Thu", "Thu 2", "Thu 3", "Thu 4", "Thu 5", "Thu 6", "Thu 7"};
//        for (int i = 1; i < 5; i++) {
//            arrSchedule[i][0] = "Ca " + i + ":";
//        }
//
//        System.out.println("Lich hoc cho: " + currentSchedule.size() + " mon");
//        for (NormalizedCourse course : currentSchedule) {
//            int shift = course.shift;
//            int dayOfWeek = shift % 4 == 0 ? shift / 4 : shift / 4 + 1;
//            int shiftOfDay = shift % 4 == 0 ? 4 : shift % 4;
//
//            // Kiểm tra chỉ số hàng và chỉ số cột
//            if (dayOfWeek >= 1 && dayOfWeek <= 7 && shiftOfDay >= 1 && shiftOfDay <= 4) {
//                arrSchedule[shiftOfDay][dayOfWeek] = course.name;
//            }
//        }
//
//        for (int i = 0; i < 5; ++i) {
//            for (int j = 0; j < 7; ++j) {
//                System.out.printf("%10s ||", arrSchedule[i][j]);
//            }
//            System.out.println();
//        }
        List<NormalizedCourse> copiedList = new ArrayList<>();
        copiedList.addAll(currentSchedule);
        listSchedule.add(copiedList);
    }

    // Hàm đệ quy để xếp lịch học trong một tuần
    static void schedule(List<NormalizedCourse> courses, List<NormalizedCourse> currentSchedule, int currentShift) {
        // Nếu đã hoàn thành xếp lịch trong cả tuần, in lịch học hiện tại và kết thúc đệ quy
        if (currentShift > 32) {
            printSchedule(currentSchedule);
            return;
        }

        // Thử thêm môn học tiếp theo vào lịch học hiện tại nếu nằm trong ngày hiện tại
        for (int i = 0; i < courses.size(); ++i) {
            NormalizedCourse nextCourse = courses.get(i);
            if (nextCourse.shift == currentShift) {
                currentSchedule.add(nextCourse);
                schedule(courses, currentSchedule, currentShift + 1);
                currentSchedule.remove(currentSchedule.size() - 1); // Quay lui để thử các lịch học khác
            }
        }

        // Quay lui để thử các lịch học khác trong ngày tiếp theo
        schedule(courses, currentSchedule, currentShift + 1);
    }

    public static void main(String[] args) {
        // Khởi tạo danh sách các môn học
        String jsonData = "[{\"name\":\"Math\",\"dayOfWeek\":\"2\",\"shift\":\"1\"},{\"name\":\"English\",\"dayOfWeek\":\"5\",\"shift\":\"2\"},{\"name\":\"Physics\",\"dayOfWeek\":\"2\",\"shift\":\"1\"},{\"name\":\"History\",\"dayOfWeek\":\"3\",\"shift\":\"3\"},{\"name\":\"Chemistry\",\"dayOfWeek\":\"3\",\"shift\":\"4\"},{\"name\":\"Biology\",\"dayOfWeek\":\"6\",\"shift\":\"2\"},{\"name\":\"Geography\",\"dayOfWeek\":\"4\",\"shift\":\"2\"}]";

        run b = new run();
        System.out.print(b.list(jsonData));
    }

    public String list(String jsonData) {
        //JSON parser object to parse read file
        JSONParser jsonParser = new JSONParser();
        List<Course> courses = new ArrayList<>();
        try {
            //Read JSON file
            Object obj = jsonParser.parse(jsonData);
            JSONArray employeeList = (JSONArray) obj;

            for (Object item : employeeList) {
                if (item instanceof JSONObject) {
                    JSONObject jsonObject = (JSONObject) item;
                    String name = jsonObject.get("name").toString();
                    int dayOfWeek = Integer.parseInt(jsonObject.get("dayOfWeek").toString());
                    int shift = Integer.parseInt(jsonObject.get("shift").toString());
                    courses.add(new Course(name, dayOfWeek, shift));
                }
            }
        } catch (ParseException e) {
            e.printStackTrace();
            return null;
        }

        // Lịch học hiện tại và ngày bắt đầu xếp lịch
        List<NormalizedCourse> currentSchedule = new ArrayList<>();
        List<NormalizedCourse> nCourse = normalize(courses);
        int startDay = 1;

        // Gọi hàm đệ quy để xếp lịch học trong tuần
        schedule(nCourse, currentSchedule, startDay);

        // Trả dữ liệu ra json
        JSONArray jsonLists = new JSONArray();
        for (List<NormalizedCourse> result : listSchedule) {
            JSONArray jsonList = new JSONArray();
            for (NormalizedCourse re : result) {
                JSONObject course = new JSONObject();
                course.put("name", re.name);
                course.put("shift", re.shift);
                jsonList.add(course);
            }
            jsonLists.add(jsonList);
        }
        listSchedule = new ArrayList<>();
        return jsonLists.toJSONString();
    }

}
