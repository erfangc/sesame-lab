import com.fasterxml.jackson.databind.ObjectMapper

val objectMapper: ObjectMapper = ObjectMapper().findAndRegisterModules()