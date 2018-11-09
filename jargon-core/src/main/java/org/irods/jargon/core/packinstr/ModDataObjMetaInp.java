package org.irods.jargon.core.packinstr;

import java.util.ArrayList;
import java.util.List;

import org.irods.jargon.core.exception.JargonException;

/**
 * XML API packing instruction for modifying data object system metadata
 *
 * @author Mike Conway - DICE (www.irods.org) FIXME: implement...
 *
 */
public class ModDataObjMetaInp extends AbstractIRODSPackingInstruction {

	private enum Mode {
		MOD_DATE
	}

	public static final String PI_TAG = "ModDataObjMeta_PI";
	public static final int MOD_DATA_OBJ_META_INP_API_NBR = 622;

	public static final String DATA_EXPIRY_KW = "dataExpiry";

	private String irodsAbsolutePath = "";
	private String dateString = "";
	private Mode mode = null;

	@Override
	public Tag getTagValue() throws JargonException {
		List<KeyValuePair> kvps = new ArrayList<KeyValuePair>();

		if (mode == Mode.MOD_DATE) {
			kvps.add(KeyValuePair.instance("dataExpiry", dateString));
		}

		DataObjInfo dataObjInfo = new DataObjInfo();
		dataObjInfo.setObjPath(this.irodsAbsolutePath);

		Tag message = new Tag(ModDataObjMetaInp.PI_TAG, new Tag[] { dataObjInfo.getTagValue() });

		message.addTag(super.createKeyValueTag(kvps));

		return message;

	}

	public ModDataObjMetaInp() {
		setApiNumber(MOD_DATA_OBJ_META_INP_API_NBR);
	}

	/**
	 * Create an instance of this packing instruction to update the expiry date for
	 * a data object given a date string
	 * 
	 * @param irodsAbsolutePath
	 *            {@code String} with the irods absolute path to the data object
	 * @param dateString
	 *            {@code String} with the date in the form expected by isysmeta
	 *            <p>
	 *            Time can be full or partial date/time: '2009-12-01' or
	 *            '2009-12-11.12:03' etc, or a delta time '+1h' (one hour from now),
	 *            etc.
	 * 
	 * 
	 * @return {@link ModDataObjMetaInp} instance
	 */
	public static final ModDataObjMetaInp instanceForModExpDate(String irodsAbsolutePath, String dateString) {
		if (irodsAbsolutePath == null || irodsAbsolutePath.isEmpty()) {
			throw new IllegalArgumentException("null or empty irodsAbsolutePath");
		}

		if (dateString == null || dateString.isEmpty()) {
			throw new IllegalArgumentException("null or empty dateString");
		}

		ModDataObjMetaInp modDataObjMetaInp = new ModDataObjMetaInp();
		modDataObjMetaInp.setDateString(dateString);
		modDataObjMetaInp.setIrodsAbsolutePath(irodsAbsolutePath);
		modDataObjMetaInp.setMode(Mode.MOD_DATE);
		return modDataObjMetaInp;

	}

	public String getIrodsAbsolutePath() {
		return irodsAbsolutePath;
	}

	public void setIrodsAbsolutePath(String irodsAbsolutePath) {
		this.irodsAbsolutePath = irodsAbsolutePath;
	}

	public String getDateString() {
		return dateString;
	}

	public void setDateString(String dateString) {
		this.dateString = dateString;
	}

	public Mode getMode() {
		return mode;
	}

	public void setMode(Mode mode) {
		this.mode = mode;
	}

}
