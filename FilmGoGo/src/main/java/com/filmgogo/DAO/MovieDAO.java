package com.filmgogo.DAO;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import com.filmgogo.VO.MovieVO;

import net.sf.json.JSONArray;

@Repository
public class MovieDAO {
	@Autowired
    private JdbcTemplate jdb;
	
	/*-----------------通过cinema id获得对应影院上映的所有电影数据，这里因为设计成所有影院都有所有电影，所以其实cinema id并没有实际用处-----------------*/
	public String getMoviesViaCid(int cid)
	{
		String sql = "select distinct movie.id, movie.name, movie.type, movie.description, movie.image, movie.score, movie.star from movie, cinema " + "where cinema.id = ?;";
		Object[] para = new Object[]{cid};
		List<MovieVO> lm = jdb.query(sql, para, new RowMapper<MovieVO>(){

			public MovieVO mapRow(ResultSet res, int arg1) throws SQLException
			{
				MovieVO m = new MovieVO();
				m.setId(res.getInt("id"));
				m.setName(res.getString("name"));
				m.setType(res.getString("type"));
				m.setDescription(res.getString("description"));
				m.setImg(res.getString("image"));
				m.setScore(res.getFloat("score"));
				m.setStar(res.getString("star"));
				return m;
			}
			
		});
		return JSONArray.fromObject(lm).toString();
	}
	
	public String getOldMovies() {
		String sql = "select id, name, description, image, score, star, type from oldmovie;";
		List<MovieVO> olm= jdb.query(sql, new RowMapper<MovieVO>(){
			public MovieVO mapRow(ResultSet res, int arg1) throws SQLException
			{
				MovieVO m = new MovieVO();
				m.setId(res.getInt("id"));
				m.setName(res.getString("name"));
				m.setDescription(res.getString("description"));
				m.setImg(res.getString("image"));
				m.setScore(res.getFloat("score"));
				m.setStar(res.getString("star"));
				m.setType(res.getString("type"));
				return m;
			}
		});
		return JSONArray.fromObject(olm).toString();
	}
	
	public String getVoteMovies() {
		String sql = "select id, name, description, image, score, star, type, votes from votemovie;";
		List<MovieVO> olm= jdb.query(sql, new RowMapper<MovieVO>(){
			public MovieVO mapRow(ResultSet res, int arg1) throws SQLException
			{
				MovieVO m = new MovieVO();
				m.setId(res.getInt("id"));
				m.setName(res.getString("name"));
				m.setDescription(res.getString("description"));
				m.setImg(res.getString("image"));
				m.setVotes(res.getInt("votes"));
				m.setScore(res.getFloat("score"));
				m.setStar(res.getString("star"));
				m.setType(res.getString("type"));
				return m;
			}
		});
		return JSONArray.fromObject(olm).toString();
	}
	
	public boolean voteMovieById(int mid, int cuid) {
		String sql = "select votes from votemovie where id= ?;";
		Object[] para = new Object[]{mid};
		List<MovieVO> vlm= jdb.query(sql, para, new RowMapper<MovieVO>(){
			public MovieVO mapRow(ResultSet res, int arg1) throws SQLException
			{
				MovieVO m = new MovieVO();
				m.setVotes(res.getInt("votes"));
				return m;
			}
		});
		if (!vlm.isEmpty()) {
			int votes= vlm.get(0).getVotes();
			votes= votes+1;
			String update_sql = "update votemovie set votes= "+votes +" where id= "+mid+";";
			jdb.update(update_sql);
			
			String insert_sql= "insert into vote (cuid, votemovieid) values(?, ?);";
			jdb.update(insert_sql, new Object[]{cuid, mid});
			//判断是否成功
			String getnew_votes= "select votes from votemovie where id= ?;";
			Object[] para2 = new Object[]{mid};
			List<MovieVO> vlm2= jdb.query(getnew_votes, para2, new RowMapper<MovieVO>(){
				public MovieVO mapRow(ResultSet res, int arg1) throws SQLException
				{
					MovieVO m = new MovieVO();
					m.setVotes(res.getInt("votes"));
					return m;
				}
			});
			int new_votes= vlm2.get(0).getVotes();
			if (new_votes == votes) return true;
			else return false;
		} else {
			return false;
		}
	}
	
	public String getVoteInfo(int cuid) {
		String sql = "select * from vote where cuid= ?;";
		Object[] para = new Object[]{cuid};
		List<MovieVO> olm= jdb.query(sql, para, new RowMapper<MovieVO>(){
			public MovieVO mapRow(ResultSet res, int arg1) throws SQLException
			{
				MovieVO m = new MovieVO();
				m.setId(res.getInt("votemovieid"));
				return m;
			}
		});
		return JSONArray.fromObject(olm).toString();
	}
	
	public boolean setVoteZero(int mid) {
		
		String getnew_votes= "select votes from votemovie where id= ?;";
		Object[] para2 = new Object[]{mid};
		List<MovieVO> vlm2= jdb.query(getnew_votes, para2, new RowMapper<MovieVO>(){
			public MovieVO mapRow(ResultSet res, int arg1) throws SQLException
			{
				MovieVO m = new MovieVO();
				m.setVotes(res.getInt("votes"));
				return m;
			}
		});
		if (!vlm2.isEmpty()) {
			String set_sql= "update votemovie set votes= "+ 0 +" where id= "+mid+";";
			jdb.update(set_sql);
			String delete_sql= "delete from vote where votemovieid= ?;";
			jdb.update(delete_sql, new Object[]{mid});
			return true;
		} else {
			return false;
		}
	}
	
}
