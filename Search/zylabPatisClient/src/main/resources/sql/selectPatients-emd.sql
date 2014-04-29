SELECT patient.patisnummer, patient.gebdatum, patient.echtgenoot, patient.patientnaam, tumoren_sec.datum_intake, patient.inschrijfdatum
FROM
(patient LEFT JOIN tumoren_prim ON patient.rec_id = tumoren_prim.patient_rec_id) LEFT JOIN tumoren_sec ON tumoren_prim.rec_id = tumoren_sec.tumoren_prim_rec_id
WHERE (
     ((patient.patientnaam) Not Like "Test*" And (patient.patientnaam) Not Like "Verwijder*" And (patient.patientnaam) Not Like "Varken*" And (patient.patientnaam) Not Like "Trani*")
AND ((patient.inschrijfdatum)>= ? OR (tumoren_sec.datum_intake) >= ?))
ORDER BY tumoren_sec.datum_intake, patient.inschrijfdatum;

