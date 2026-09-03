//package vn.rikkei.exam.trainingroom.service.mcp.tools;
//
//import org.springframework.ai.mcp.annotation.McpTool;
//import org.springframework.ai.mcp.annotation.McpToolParam;
//import org.springframework.stereotype.Component;
//import ra.demo.dto.StudentExamMarkDTO;
//import ra.demo.entity.Student;
//import ra.demo.repository.ClassRepository;
//import ra.demo.repository.ExamMarkRepository;
//import ra.demo.repository.StudentRepository;
//import ra.demo.repository.SubjectRepository;
//
//import java.util.List;
//
//@Component
//public class StudentMarkTools {
//    private final StudentRepository studentRepository;
//    private final SubjectRepository subjectRepository;
//    private final ExamMarkRepository examMarkRepository;
//    private final ClassRepository classRepository;
//
//    public StudentMarkTools(StudentRepository studentRepository, SubjectRepository subjectRepository, ExamMarkRepository examMarkRepository, ClassRepository classRepository) {
//        this.studentRepository = studentRepository;
//        this.subjectRepository = subjectRepository;
//        this.examMarkRepository = examMarkRepository;
//        this.classRepository = classRepository;
//    }
//
//    @McpTool(
//            name = "get_student_by_student_code",
//            description = "Lấy thông tin sinh viên theo mã sinh viên"
//    )
//    public Student getStudentById(
//            @McpToolParam(
//                    description = "Mã sinh viên",
//                    required = true
//            )
//            String studentCode) {
//        return studentRepository.findByStudentCode(studentCode).orElse(null);
//    }
//
//    @McpTool(
//            name = "get_students_by_class_code",
//            description = "Lấy danh sách sinh viên theo lớp học"
//    )
//    public List<Student>  getStudentByClassId(
//            @McpToolParam(
//                    description = "Mã lớp học",
//                    required = true
//            )
//            String classCode
//    ){
//        return studentRepository.findAllByClassEntity_ClassCode(classCode);
//    }
//
//    @McpTool(
//            name = "get_student_exam_mark",
//            description = "Lấy thông tin điểm thi của sinh viên"
//    )
//    public List<StudentExamMarkDTO> getStudentExamMark(){
//        return studentRepository.getStudentExamMarkDTO();
//    }
//}
