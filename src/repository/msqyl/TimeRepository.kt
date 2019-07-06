package repository.msqyl

import exception.NoDataException
import io.ktor.util.date.WeekDay
import model.BusStop
import model.BusType
import model.Time
import model.WeekType
import org.slf4j.LoggerFactory
import java.sql.Connection
import java.sql.PreparedStatement
import java.sql.ResultSet
import java.sql.SQLException
import java.time.LocalTime
import java.util.*


class TimeRepository(argDB: Connection) : TimeRepositoryInterface {
    private val db: Connection = argDB
    private val log = LoggerFactory.getLogger("TimeRepository")

    override fun findNext(): Time {
        var result: Time? = null
        val now = LocalTime.now()
        val cal = Calendar.getInstance(TimeZone.getTimeZone("Asia/Tokyo"), Locale.JAPAN)
        val weekDay = WeekDay.from(cal.get(Calendar.DAY_OF_WEEK) - 1)
        var query =
            "SELECT * FROM time_table WHERE (hour = ? AND minutes > ?) OR hour > ? AND weekday = ? OR weekday = 2 ORDER BY hour ASC,minutes ASC LIMIT 1"
        var weekType = if (weekDay == WeekDay.SUNDAY || weekDay == WeekDay.SATURDAY) 1 else 0
        lateinit var stmt: PreparedStatement

        try {
            stmt = db.prepareStatement(query)
            stmt.setInt(1, now.hour)
            stmt.setInt(2, now.minute)
            stmt.setInt(3, weekType)
            val resultSet: ResultSet? = stmt.executeQuery()
            resultSet?.next()
            result = Time(
                id = resultSet?.getInt("id") ?: throw NoDataException(),
                time = LocalTime.of(resultSet.getInt("hour"), resultSet.getInt("minutes")),
                weekDay = WeekType.fromValue(resultSet.getInt("weekday")),
                type = BusType.fromID(resultSet.getInt("type")),
                from = BusStop.fromID(resultSet.getInt("from")),
                to = BusStop.fromID(resultSet.getInt("to"))
            )
        } catch (e: NoDataException) {
            log.error("NoDataException,", e.printStackTrace())
        } catch (e: SQLException) {
            log.error("Mysql exception:", e.printStackTrace())
        } catch (e: Exception) {
            log.error(e.printStackTrace().toString())
            // 出るとは思わないが取りえず
        } finally {
            try {
                stmt.close()
                db.close()
            } catch (e: Exception) {
                log.error("Mysql exception:", e.printStackTrace())
            }
        }
        // nullだったら空構造体を返す
        return result ?: Time()
    }

    override fun findOne(id: Int): Time {
        return Time()
    }
}

private fun Any.setInt(hour: Int, minute: Int, weekType: Int) {

}
