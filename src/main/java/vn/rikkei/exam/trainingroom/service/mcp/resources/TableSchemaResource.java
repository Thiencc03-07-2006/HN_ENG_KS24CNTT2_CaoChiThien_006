package vn.rikkei.exam.trainingroom.service.mcp.resources;

import org.springframework.ai.mcp.annotation.McpResource;
import org.springframework.stereotype.Component;

@Component
public class TableSchemaResource {
    @McpResource(
            uri = "c://db/db-schema",
            name = "student-database-schema",
            description = "Cấu trúc bảng dữ liệu trong csdl mysql",
            mimeType = "application/json"
    )
    public String schemaResource(){
        return """
                {
                    "tables":[
                        {
                            "Students": {
                                "student_id":"INT (Id sinh viên)",
                                "student_code:"VARCHAR (Mã sinh viên)",
                                "full_name":"VARCHAR (Họ tên sinh viên)",
                                "date_of_birthday":"DATE (Ngày sinh)",
                                "gender":"VARCHAR (Giới tính)",
                                "email":"VARCHAR (Địa chỉ mail)",
                                "class_id":"INT (Khóa ngoại với bảng classes (Lớp học))"
                            },
                            "Classes":{
                                "class_id":"INT (Id lớp học)",
                                "class_code":"VARCHAR (Mã lớp học)",
                                "class_name":"VARCHAR (Tên lớp học)",
                                "major":"VARCHAR (Ngành học)",
                                "academic_year":"VARCHAR (Năm học)"
                            },
                            "Subjects":{
                                "subject_id":"INT (Id môn học)",
                                "subject_code":"VARCHAR (Mã môn học)",
                                "subject_name":"VARCHAR (Tên môn học)",
                                "credits":"INT (Học kỳ)"
                            },
                            "ExamMark":{
                                "exam_mark_id":"INT (Mã điểm thi)",
                                "student_id":"INT (Mã sinh viên - khóa ngoại với bảng students)",
                                "subject_id":"INT (Mã môn học - khóa ngoại với bảng subjects)",
                                "exam_date":"DATE (Ngày thi)",
                                "mark":"DECIMAL (điểm thi)"
                            }
                        }
                    ]
                }
                """;
    }
}
