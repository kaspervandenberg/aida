SELECT     RTRIM(r.descp) AS UserName
, s.start_dtime AS StartTime
, s.end_dtime AS EndTime
, i.proc_desc_short AS ActivityName
, RTRIM(p.pt_med_rec_no) AS PatientId
FROM         visit_activity AS v INNER JOIN
                      item AS i ON v.proc_no = i.proc_no INNER JOIN
                      schdtl_info AS si ON v.acc_itn = si.acc_itn INNER JOIN
                      schdtl AS s ON s.acc_itn = v.acc_itn INNER JOIN
                      resource AS r ON si.primary_resource = r.internal_key INNER JOIN
                      schdtlhdr AS rr ON rr.acc_itn = v.acc_itn AND s.acc_itn = rr.acc_itn INNER JOIN
                      patient AS p ON v.pat_itn = p.pat_itn
WHERE     (s.dept = 'ONC') AND (rr.rtype = 'P')
and s.start_dtime > '2014-04-01 00:00:00'
and s.start_dtime < '2014-04-02 00:00:00'
and (i.proc_desc_short like '%INTAKE%' OR i.proc_desc_short like '%INSCHRIJF%')
ORDER BY s.start_dtime

