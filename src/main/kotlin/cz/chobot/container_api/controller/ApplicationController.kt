//package cz.chobot.container_api.controller
//
//import cz.chobot.container_api.bo.userApplication.ApplicationDto
//import cz.chobot.container_api.repository.ApplicationDao
//import cz.chobot.container_api.service.IApplicationService
//import org.springframework.beans.factory.annotation.Autowired
//import org.springframework.http.ResponseEntity
//import org.springframework.web.bind.annotation.*
//import org.springframework.web.servlet.support.ServletUriComponentsBuilder
//
//
//@RestController
//@RequestMapping("/api/v1/user/{idUser}/application")
//class ApplicationController {
//
//    @Autowired
//    private lateinit var applicationDao: ApplicationDao
//
//    @Autowired
//    private lateinit var applicationService: IApplicationService
//
//    @GetMapping("{idApplication}")
//    fun getUserApplication(@PathVariable("idUser") idUser: Long, @PathVariable("idApplication") idApplication: Long): ResponseEntity<ApplicationDto> {
//        return applicationDao.findByIdAndUser_Id(idApplication, idUser).map { app ->
//            ResponseEntity.ok(app)
//        }.orElse(ResponseEntity.notFound().build())
//    }
//
//    @PostMapping
//    fun createApplication(@PathVariable("idUser") idUser: Long, @RequestBody application: ApplicationDto): ResponseEntity<Void> {
//        val newApplication = applicationService.createApplication(application)
//        val location = ServletUriComponentsBuilder.fromCurrentRequest()
//                .path("/{id}")
//                .buildAndExpand(newApplication.id)
//                .toUri()
//        return ResponseEntity.created(location).build()
//    }
//
//}