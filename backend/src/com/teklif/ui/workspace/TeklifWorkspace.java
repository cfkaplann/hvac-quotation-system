package com.teklif.ui.workspace;

import com.teklif.model.dto.PricingRequest;

import com.teklif.ui.mapper.OlcuRequestMapper;

import javax.swing.*;
import java.awt.*;
import java.util.*;
import java.util.List;

import com.teklif.model.OlcuAlanTipi;
import com.teklif.model.OzellikTipi;
import com.teklif.model.ParaBirimi;
import com.teklif.model.UrunKart;
import com.teklif.model.UrunKategori;
import com.teklif.repository.UrunKataloguDeposu;

import com.teklif.ui.component.OlcuComponent;
import com.teklif.ui.component.RalCompositeComponent;
import com.teklif.ui.erp.ActionBarPanel;
import com.teklif.ui.erp.DynamicFormPanel;
import com.teklif.ui.erp.ToolbarPanel;
import com.teklif.ui.factory.OlcuPanelFactory;
import com.teklif.ui.factory.OzellikPanelFactory;
import com.teklif.ui.table.TeklifTablePanel;

import com.teklif.controller.TeklifController;
import com.teklif.pricing.dto.PricingResult;
import com.teklif.ui.mapper.OzellikRequestMapper;
import com.teklif.ui.resolver.GosterimKodResolver;

public class TeklifWorkspace extends JPanel {

	private final ToolbarPanel toolbar;
	private final DynamicFormPanel form;
	private final ActionBarPanel actions;
	private final TeklifTablePanel table;
	private TeklifController controller = new TeklifController();
	private static final boolean DEBUG = false;
	
	private JPanel motorContainer;
	private Map<String, JTextField> motorInputs = new HashMap<>();

	private final List<OlcuComponent> olcuComponents = new ArrayList<>();
	private final Map<OzellikTipi, JComponent> ozellikInputs = new HashMap<>();

	private JTextField txtMiktar;

	public TeklifWorkspace() {
		
		

		setLayout(new BorderLayout());

		toolbar = new ToolbarPanel();
		form = new DynamicFormPanel();
		actions = new ActionBarPanel();
		table = new TeklifTablePanel();
		
		
		motorContainer = new JPanel();
		motorContainer.setLayout(new BoxLayout(motorContainer, BoxLayout.Y_AXIS));
		motorContainer.setVisible(false);

		// ⭐ motor alanını teknik panele ekliyoruz
		form.getPnlTeknik().add(motorContainer);

		// ⭐ başlangıçta gizli
		form.setVisible(false);
		actions.setVisible(false);

		add(toolbar, BorderLayout.NORTH);

		JPanel center = new JPanel(new BorderLayout());
		center.add(form, BorderLayout.CENTER);
		center.add(actions, BorderLayout.SOUTH);

		JSplitPane split = new JSplitPane(JSplitPane.VERTICAL_SPLIT, center, table);

		split.setResizeWeight(0.65);
		split.setContinuousLayout(true);
		split.setOneTouchExpandable(true);
		split.setDividerSize(4);

		// ⭐ PROGRAM AÇILIŞ YÜKSEKLİĞİ
		SwingUtilities.invokeLater(() -> {
		    split.setDividerLocation(175); // burayı istediğin kadar büyüt
		});

		add(split, BorderLayout.CENTER);


		initKategori();

		toolbar.getCmbKategori().addActionListener(e -> kategoriDegisti());
		toolbar.getCmbUrun().addActionListener(e -> urunDegisti());
		
		toolbar.getCmbParaBirimi().addActionListener(e -> {

		    ParaBirimi pb =
		            (ParaBirimi) toolbar.getCmbParaBirimi().getSelectedItem();

		    table.setParaBirimi(pb);
		});

		actions.getBtnUrunEkle().addActionListener(e -> urunEkle());
		actions.getBtnUrunSil().addActionListener(e -> table.removeSelected());
	}

	// =====================================================
	// ⭐ KATEGORI INIT
	// =====================================================

	private void initKategori() {

	    toolbar.getCmbKategori().removeAllItems();

	    // ✅ placeholder
	    toolbar.getCmbKategori().addItem(null);

	    for (UrunKategori k : UrunKataloguDeposu.tumKategoriler()) {
	        toolbar.getCmbKategori().addItem(k);
	    }

	    toolbar.getCmbUrun().removeAllItems();
	    toolbar.getCmbUrun().addItem(null);   // ✅ placeholder artık null
	    toolbar.getCmbUrun().setEnabled(false);
	}

	// =====================================================
	// ⭐ KATEGORI DEĞİŞTİ
	// =====================================================

	private void kategoriDegisti() {

	    toolbar.getCmbUrun().removeAllItems();
	    toolbar.getCmbUrun().addItem(null); // ✅ placeholder
	    toolbar.getCmbUrun().setEnabled(false);

	    UrunKategori kat = (UrunKategori) toolbar.getCmbKategori().getSelectedItem();

	    // ✅ Kategori seçilmediyse reset
	    if (kat == null) {

	        form.getPnlOlculer().removeAll();
	        form.getPnlTeknik().removeAll();
	        form.getPnlRalAksesuar().removeAll();

	        olcuComponents.clear();
	        ozellikInputs.clear();

	        form.setVisible(false);
	        actions.setVisible(false);

	        revalidate();
	        repaint();
	        return;
	    }

	    // ✅ Ürünleri doldur
	    for (UrunKart u : UrunKataloguDeposu.tumUrunler()) {
	        if (u.getKategori() == kat) {
	            toolbar.getCmbUrun().addItem(u);
	        }
	    }

	    toolbar.getCmbUrun().setEnabled(true);
	}

	// =====================================================
	// ⭐ URUN DEĞİŞTİ
	// =====================================================

	private void urunDegisti() {

	    UrunKart kart = (UrunKart) toolbar.getCmbUrun().getSelectedItem();

	    // ✅ Ürün seçilmediyse kapat
	    if (kart == null) {
	        form.setVisible(false);
	        actions.setVisible(false);
	        return;
	    }

	    form.setVisible(true);
	    actions.setVisible(true);

	    rebuildForm(kart);
	}

	// =====================================================
	// ⭐ FORM BUILD
	// =====================================================

	private void rebuildForm(UrunKart kart) {

		JPanel pnlOlcu = form.getPnlOlculer();
		JPanel pnlTeknik = form.getPnlTeknik();
		JPanel pnlRal = form.getPnlRalAksesuar();

		pnlOlcu.removeAll();
		pnlTeknik.removeAll();
		pnlRal.removeAll();

		olcuComponents.clear();
		ozellikInputs.clear();

		// =====================================================
		// ⭐ ÖLÇÜLER
		// =====================================================

		OlcuComponent kasaComp = null;
		OlcuComponent bogazComp = null;

		for (OlcuAlanTipi tip : kart.getZorunluOlculer()) {

			OlcuComponent comp = OlcuPanelFactory.createComponent(tip);

			// ⭐ LABEL OVERRIDE (UI only)
			if (kart.getOlcuLabelMap() != null) {
			    String yeniLabel = kart.getOlcuLabelMap().get(tip);
			    if (yeniLabel != null && !yeniLabel.isBlank()) {
			        comp.setLabelText(yeniLabel);
			    }
			}

			// ⭐ CONFIGTEN COMBO DEĞERLERİ BAS
			if (kart.getIzinliOlcuDegerleri() != null) {

				java.util.List<String> values = kart.getIzinliOlcuDegerleri().get(tip);

				if (values != null) {
					comp.setAllowedValues(values);
				}
			}

			// ⭐ REFERANS YAKALA
			if (tip == OlcuAlanTipi.KASA_WH)
				kasaComp = comp;
			if (tip == OlcuAlanTipi.BOGAZ_WH)
				bogazComp = comp;

			pnlOlcu.add(comp);
			olcuComponents.add(comp);
		}

		// =====================================================
		// ⭐ KASA → BOĞAZ FİLTRE SİSTEMİ
		// =====================================================

		if (kasaComp != null && bogazComp != null && kart.getBogazFiltreMap() != null
				&& kart.getZorunluOlculer().contains(OlcuAlanTipi.BOGAZ_WH)) {

			OlcuComponent finalKasa = kasaComp;
			OlcuComponent finalBogaz = bogazComp;

			Runnable filtreUygula = () -> {

				String secilenKasa = finalKasa.getValue();

				List<String> izinliBogaz = kart.getBogazFiltreMap().get(secilenKasa);

				if (izinliBogaz != null) {
					finalBogaz.setAllowedValues(izinliBogaz);
				}
			};

			// ⭐ kullanıcı değiştirince
			finalKasa.addChangeListener(filtreUygula);

			// ⭐ ürün ilk açıldığında da tetikle
			SwingUtilities.invokeLater(filtreUygula);

			// ⭐⭐⭐ BOĞAZ DEFAULT SEÇİM GARANTİ ⭐⭐⭐
			SwingUtilities.invokeLater(() -> {

				String val = finalBogaz.getValue();

				if (val == null || val.isBlank()) {

					List<String> tumBogaz = (kart.getIzinliOlcuDegerleri() == null) ? null
							: kart.getIzinliOlcuDegerleri().get(OlcuAlanTipi.BOGAZ_WH);

					if (tumBogaz != null && !tumBogaz.isEmpty()) {
						finalBogaz.setValue(tumBogaz.get(0));
					}
				}
			});

		}

		// =====================================================
		// ⭐ MİKTAR ALANI
		// =====================================================

		JPanel miktarPanel = new JPanel(new GridBagLayout());

		GridBagConstraints gbc = new GridBagConstraints();
		gbc.insets = new Insets(2, 2, 2, 2);
		gbc.anchor = GridBagConstraints.WEST;

		JLabel lbl = new JLabel("Miktar");
		lbl.setPreferredSize(new Dimension(95, 22));

		gbc.gridx = 0;
		miktarPanel.add(lbl, gbc);

		txtMiktar = new JTextField();
		txtMiktar.setPreferredSize(new Dimension(75, 22));

		gbc.gridx = 1;
		miktarPanel.add(txtMiktar, gbc);

		pnlOlcu.add(miktarPanel);

		txtMiktar.setDocument(new javax.swing.text.PlainDocument() {
			@Override
			public void insertString(int offs, String str, javax.swing.text.AttributeSet a)
					throws javax.swing.text.BadLocationException {

				if (str == null)
					return;

				if (str.matches("[0-9]*")) {
					super.insertString(offs, str, a);
				}
			}
		});

		// =====================================================
		// ⭐ ÖZELLİKLER
		// =====================================================

		ozellikInputs.putAll(OzellikPanelFactory.createERPColumns(pnlTeknik, pnlRal, kart, () -> {
		}));
		
		// ⭐ ürün değişince motor alanını resetle
		motorContainer.removeAll();
		motorContainer.setVisible(false);
		motorInputs.clear();
		
		// ⭐ motor panelini tekrar ekle (removeAll sonrası)
		pnlTeknik.add(motorContainer, 0);
		
		// =====================================================
		// ⭐ AKSESUAR RADIO LISTENER (MOTOR FİYAT ALANI)
		// =====================================================

		JComponent comp = ozellikInputs.get(OzellikTipi.AKSESUAR_TIPI);

		if(comp instanceof JPanel){

		    JPanel grid = (JPanel) comp;

		    for(Component c : grid.getComponents()){

		        if(c instanceof JRadioButton){

		            JRadioButton rb = (JRadioButton) c;

		            rb.addActionListener(e -> updateMotorFields(grid));
		        }
		    }
		}

		revalidate();
		repaint();
	}

	// =====================================================
	// ⭐ URUN EKLE
	// =====================================================

	private void urunEkle() {

		this.requestFocusInWindow();

		if (!formValidMi()) {
			JOptionPane.showMessageDialog(this, "Tüm alanları doldurmadan ürün ekleyemezsiniz.", "Eksik Bilgi",
					JOptionPane.WARNING_MESSAGE);
			return;
		}

		Object selected = toolbar.getCmbUrun().getSelectedItem();

		if (!(selected instanceof UrunKart))
			return;

		UrunKart kart = (UrunKart) selected;

		String miktarStr = txtMiktar.getText();

		int miktar = Integer.parseInt(miktarStr);

		if (miktar <= 0)
			throw new RuntimeException("Miktar 0'dan büyük olmalı");

		// =====================================================
		// ⭐ PRICING REQUEST BUILD
		// =====================================================

		PricingRequest.Builder builder = PricingRequest.builder(kart.getKod());

		// ölçüler mapper
		OlcuRequestMapper.map(builder, olcuComponents);

		PricingRequest req = builder.build();

		// =====================================================
		// ⭐ ÖZELLİK MAP
		// =====================================================

		Map<OzellikTipi, List<String>> secimler = OzellikRequestMapper.map(ozellikInputs);
		
		String ozellikSuffix = buildOzellikSuffix(secimler);

		if (DEBUG) {
		    System.out.println(secimler);
		}

		// =====================================================
		// ⭐ CONTROLLER ÜZERİNDEN HESAPLA
		// =====================================================

		// =====================================================
		// ⭐ MOTOR FİYATLARINI TOPLA
		// =====================================================

		Map<String, Double> motorFiyatlari = new HashMap<>();

		for(Map.Entry<String, JTextField> e : motorInputs.entrySet()){

		    String val = e.getValue().getText();

		    if(val != null && !val.isBlank()){
		    	motorFiyatlari.put(
		    		    e.getKey(),
		    		    Double.parseDouble(val.replace(",", "."))
		    		);
		    }
		}

		// =====================================================
		// ⭐ CONTROLLER ÜZERİNDEN HESAPLA
		// =====================================================

		
		// ⭐ SADECE GÖRÜNTÜ İÇİN
		ParaBirimi pb =
		        (ParaBirimi) toolbar.getCmbParaBirimi().getSelectedItem();

		table.setParaBirimi(pb);

		// ⭐ FİYATI HER ZAMAN TL HESAPLA
		PricingResult tlResult =
		        controller.hesapla(req, secimler, motorFiyatlari);

		// ⭐ TABLOYA TL YAZ
		double birimFiyat = tlResult.getToplam();
		double toplamFiyat = birimFiyat * miktar;

		String genislik = "";
		String yukseklik = "";
		String uzunluk = "";
		String cap = "";

		for (OlcuComponent c : olcuComponents) {

			if (c.getTip() == OlcuAlanTipi.GENISLIK)
				genislik = c.getValue();

			if (c.getTip() == OlcuAlanTipi.YUKSEKLIK)
				yukseklik = c.getValue();

			if (c.getTip() == OlcuAlanTipi.UZUNLUK)
				uzunluk = c.getValue();

			if (c.getTip() == OlcuAlanTipi.CAP || c.getTip() == OlcuAlanTipi.BOGAZ_CAP
					|| c.getTip() == OlcuAlanTipi.NETIC_CAP)
				cap = c.getValue();
		}

		String cerceve = "";
		String damper = "";
		String ral = "";
		String montaj = "";

		for (Map.Entry<OzellikTipi, JComponent> e : ozellikInputs.entrySet()) {

			JComponent comp = e.getValue();

			// ⭐ NORMAL COMBO'LAR
			if (comp instanceof JComboBox) {

				Object val = ((JComboBox<?>) comp).getSelectedItem();

				if (val == null)
					continue;

				switch (e.getKey()) {

				case CERCEVE_TIPI:
					cerceve = val.toString();
					break;
				case DAMPER_TIPI:
					damper = val.toString();
					break;
				case MONTAJ:
					montaj = val.toString();
					break;
				}
			}

			// ⭐ YENİ RAL COMPOSITE COMPONENT
			if (e.getKey() == OzellikTipi.RAL && comp instanceof RalCompositeComponent) {

				RalCompositeComponent ralComp = (RalCompositeComponent) comp;

				ral = ralComp.getValue(); // ⭐ Boyalı - 9010 burada oluşuyor
			}
		}

		// =====================================================
		// ⭐ SLOT ise ürün adının başına: "{slot} Yarıklı " ekle
		// =====================================================
		String anaAd = kart.getAd();

		if (kart.getKod() != null && kart.getKod().startsWith("SLT")) {

		    String slotSayisi = "";

		    for (OlcuComponent c : olcuComponents) {
		        if (c.getTip() == OlcuAlanTipi.SLOT_SAYISI) {
		            slotSayisi = c.getValue();
		            break;
		        }
		    }

		    if (slotSayisi != null && !slotSayisi.isBlank()) {
		        anaAd = slotSayisi + " Yarıklı " + anaAd;
		    }
		}
		
		String urunAdiGosterim =
		        "<html>"
		        + anaAd
		        + buildUrunAdiOlcuEki()
		        + ozellikSuffix
		        + "</html>";
		
		Object[] row = { GosterimKodResolver.resolve(kart.getKod()), urunAdiGosterim, genislik, yukseklik, uzunluk, cap, cerceve, damper, ral, montaj, miktarStr,
				"Adet", birimFiyat, toplamFiyat };

		table.addRow(row);
	}

	// =====================================================
	// ⭐ VALIDATION
	// =====================================================

	private boolean formValidMi() {

		for (OlcuComponent c : olcuComponents) {

			String v = c.getValue();

			if (v == null || v.isBlank())
				return false;

			// ✅ KASA/BOĞAZ string olduğu için sayı kontrolü YAPMA
			if (c.getTip() == OlcuAlanTipi.KASA_WH || c.getTip() == OlcuAlanTipi.BOGAZ_WH
					|| c.getTip() == OlcuAlanTipi.KASA_CAP) {
				continue;
			}

			// ✅ Diğerleri sayısal (W,H,L,Ø,slot vs.)
			try {
				if (c.getDoubleValue() <= 0)
					return false;
			} catch (Exception ex) {
				return false;
			}
		}

		// ✅ Miktar kontrolü
		if (txtMiktar == null || txtMiktar.getText().isBlank())
			return false;

		try {
			if (Integer.parseInt(txtMiktar.getText()) <= 0)
				return false;
		} catch (Exception e) {
			return false;
		}

		// ✅ Combo özellikler boş mu
		for (JComponent comp : ozellikInputs.values()) {

			if (comp instanceof JComboBox) {

				Object val = ((JComboBox<?>) comp).getSelectedItem();

				if (val == null || "Seçim Yapınız".equals(val.toString()))
					return false;
			}

			if (comp instanceof RalCompositeComponent) {

				RalCompositeComponent rc = (RalCompositeComponent) comp;

				String val = rc.getValue();

				if (val == null || val.isBlank())
					return false;

				
			}

		}

		return true;
	}
	
	// =====================================================
	// ⭐ Ürün adı yanına ölçü eklemek için (KASA/BOĞAZ gibi)
	// =====================================================
	private String buildUrunAdiOlcuEki() {

	    String kasaWh = "";
	    String bogazWh = "";
	    String kasaCap = "";

	    for (OlcuComponent c : olcuComponents) {

	        if (c.getTip() == OlcuAlanTipi.KASA_WH) {
	            kasaWh = c.getValue();
	        }

	        if (c.getTip() == OlcuAlanTipi.BOGAZ_WH) {
	            bogazWh = c.getValue();
	        }

	        if (c.getTip() == OlcuAlanTipi.KASA_CAP) {
	            kasaCap = c.getValue();  // örn: "Ø300"
	        }
	    }

	    // hiçbir ek yoksa boş dön
	    if ((kasaWh == null || kasaWh.isBlank()) &&
	        (bogazWh == null || bogazWh.isBlank()) &&
	        (kasaCap == null || kasaCap.isBlank())) {
	        return "";
	    }

	    // öncelik: KASA/BOĞAZ (anemostat gibi)
	    if (kasaWh != null && !kasaWh.isBlank() && bogazWh != null && !bogazWh.isBlank()) {
	        return " (" + kasaWh + "/" + bogazWh + ")";
	    }

	    // sadece kasa varsa
	    if (kasaWh != null && !kasaWh.isBlank()) {
	        return " (" + kasaWh + ")";
	    }

	    // sadece boğaz varsa
	    if (bogazWh != null && !bogazWh.isBlank()) {
	        return " (" + bogazWh + ")";
	    }

	    // dairesel gibi kasa çapı varsa
	    if (kasaCap != null && !kasaCap.isBlank()) {
	        return " (" + kasaCap + ")";
	    }

	    return "";
	}

	// =====================================================
	// ⭐ Ürün adı altına özellik satırları ekler
	// =====================================================
	private String buildOzellikSuffix(Map<OzellikTipi, List<String>> secimler){

	    StringBuilder sb = new StringBuilder();

	    List<String> menfezList = secimler.get(OzellikTipi.MENFEZ_TIPI);
	    List<String> aksesuarList = secimler.get(OzellikTipi.AKSESUAR_TIPI);

	    if(menfezList != null && !menfezList.isEmpty()){
	    	sb.append("<br>");
	        sb.append(String.join(", ", menfezList));
	    }

	    if(aksesuarList != null && !aksesuarList.isEmpty()){
	    	sb.append("<br>");
	        sb.append(String.join(", ", aksesuarList));
	    }

	    return sb.toString();
	}
	
	private void updateMotorFields(JPanel grid){

	    motorContainer.removeAll();
	    motorInputs.clear();

	    boolean enAzBirMotorVar = false;

	    for(Component c : grid.getComponents()){

	        if(c instanceof JRadioButton){

	            JRadioButton rb = (JRadioButton) c;

	            if(rb.isSelected()){

	                String secim = rb.getText();

	                boolean fiyatliAksesuar =
	                        secim.equalsIgnoreCase("Servo Motor 24V") ||
	                        secim.equalsIgnoreCase("Servo Motor 230V");

	                if(fiyatliAksesuar){

	                    enAzBirMotorVar = true;

	                    JPanel row = new JPanel(new FlowLayout(FlowLayout.LEFT));

	                    row.add(new JLabel(secim + " Fiyatı:"));

	                    JTextField tf = new JTextField(8);
	                    row.add(tf);

	                    motorContainer.add(row);

	                    motorInputs.put(secim, tf);
	                }
	            }
	        }
	    }

	    motorContainer.setVisible(enAzBirMotorVar);

	    motorContainer.revalidate();
	    motorContainer.repaint();
	    
	    form.revalidate();
	    form.repaint();
	}
}
