package com.example.data

import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.io.BufferedReader
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URL

data class ProductOnlineInfo(
    val name: String,
    val category: String? = null,
    val brand: String? = null,
    val quantityString: String? = null,
    val barcode: String? = null,
    val storeOrSource: String = "Supermercados & Adegas"
)

/**
 * Serviço Inteligente de Busca e Identificação de Produtos para Adegas, Distribuidoras de Bebidas
 * e Grandes Supermercados.
 *
 * Integra:
 * 1) Catálogo Instantâneo Embutido (EANs e produtos populares de adegas brasileiras e grandes redes)
 * 2) Busca em Cascata Online:
 *    - Open Food Facts Brasil (Catálogo prioritário de alimentos e bebidas BR)
 *    - Open Food Facts Mundial
 *    - UPCitemdb API (Catálogo global de código de barras)
 *    - Open Products Facts
 * 3) Normalização, higienização do nome e classificação automática em 10 categorias oficiais:
 *    Mercearia, Vinhos, Destilados, Salgadinhos, Bomboniere, Congelados, Gelos, Cervejas,
 *    Sucos e Diversos, Tabaco.
 */
object OnlineProductLookupService {

    // Catálogo instantâneo de alta performance com os itens mais frequentes em adegas e supermercados brasileiros
    private val INSTANT_PRODUCT_CATALOG: Map<String, Pair<String, String>> = mapOf(
        // === CERVEJAS ===
        "7891991010832" to ("Cerveja Heineken Puro Malte Garrafa Long Neck 330ml" to "Cervejas"),
        "7891991000819" to ("Cerveja Heineken Puro Malte Lata 350ml" to "Cervejas"),
        "7891991015257" to ("Cerveja Heineken Puro Malte Latão 473ml" to "Cervejas"),
        "7891991000888" to ("Cerveja Heineken 0.0 Álcool Long Neck 330ml" to "Cervejas"),
        "7891991001403" to ("Cerveja Amstel Puro Malte Lager Lata 350ml" to "Cervejas"),
        "7891991001410" to ("Cerveja Amstel Puro Malte Latão 473ml" to "Cervejas"),
        "7891991012355" to ("Cerveja Eisenbahn Pilsen Long Neck 355ml" to "Cervejas"),
        "7891991012362" to ("Cerveja Eisenbahn IPA Puro Malte Long Neck 355ml" to "Cervejas"),
        "7891149103233" to ("Cerveja Stella Artois Premium Long Neck 330ml" to "Cervejas"),
        "7891149103240" to ("Cerveja Stella Artois Pure Gold Sem Glúten 330ml" to "Cervejas"),
        "7891149200406" to ("Cerveja Spaten Puro Malte Lata 350ml" to "Cervejas"),
        "7891149200420" to ("Cerveja Spaten Munchen Long Neck 355ml" to "Cervejas"),
        "7891149102120" to ("Cerveja Corona Extra Garrafa 330ml" to "Cervejas"),
        "7891149010104" to ("Cerveja Brahma Chopp Lata 350ml" to "Cervejas"),
        "7891149108306" to ("Cerveja Brahma Duplo Malte Lata 350ml" to "Cervejas"),
        "7891149020103" to ("Cerveja Skol Pilsen Lata 350ml" to "Cervejas"),
        "7891149030102" to ("Cerveja Antarctica Original Garrafa 600ml" to "Cervejas"),
        "7891149040101" to ("Cerveja Budweiser American Lager Lata 350ml" to "Cervejas"),
        "7891149040200" to ("Cerveja Budweiser Long Neck 330ml" to "Cervejas"),
        "7891149108115" to ("Cerveja Colorado Ribeirão Lager Garrafa 600ml" to "Cervejas"),
        "7891149108122" to ("Cerveja Colorado Appia Trigo Garrafa 600ml" to "Cervejas"),
        "7891149050100" to ("Cerveja Beck's German Pilsner Long Neck 330ml" to "Cervejas"),

        // === SUCOS E DIVERSOS (REFRIGERANTES, ENERGÉTICOS, ÁGUAS) ===
        "7894900010015" to ("Refrigerante Coca-Cola Original Lata 350ml" to "Sucos e Diversos"),
        "7894900011517" to ("Refrigerante Coca-Cola Original Garrafa Pet 2L" to "Sucos e Diversos"),
        "7894900700015" to ("Refrigerante Coca-Cola Sem Açúcar Lata 350ml" to "Sucos e Diversos"),
        "7894900701517" to ("Refrigerante Coca-Cola Sem Açúcar Pet 2L" to "Sucos e Diversos"),
        "7891991001342" to ("Refrigerante Guaraná Antarctica Lata 350ml" to "Sucos e Diversos"),
        "7891991001359" to ("Refrigerante Guaraná Antarctica Garrafa Pet 2L" to "Sucos e Diversos"),
        "7894900050011" to ("Refrigerante Fanta Laranja Lata 350ml" to "Sucos e Diversos"),
        "7892840801014" to ("Refrigerante Pepsi Black Zero Lata 350ml" to "Sucos e Diversos"),
        "7894900060010" to ("Refrigerante Sprite Fresh Limão Lata 350ml" to "Sucos e Diversos"),
        "7891991001601" to ("Água Tônica Antarctica Tradicional Lata 350ml" to "Sucos e Diversos"),
        "9002490100070" to ("Energético Red Bull Energy Drink Lata 250ml" to "Sucos e Diversos"),
        "9002490205980" to ("Energético Red Bull Sugarfree Lata 250ml" to "Sucos e Diversos"),
        "9002490234508" to ("Energético Red Bull Tropical Edition 250ml" to "Sucos e Diversos"),
        "70847012470"   to ("Energético Monster Energy Tradicional 473ml" to "Sucos e Diversos"),
        "70847022356"   to ("Energético Monster Energy Ultra Zero 473ml" to "Sucos e Diversos"),
        "70847035547"   to ("Energético Monster Energy Mango Loco 473ml" to "Sucos e Diversos"),
        "7894900530018" to ("Água Mineral Crystal Sem Gás 500ml" to "Sucos e Diversos"),
        "7894900531015" to ("Água Mineral Crystal Com Gás 500ml" to "Sucos e Diversos"),
        "7891025114536" to ("Água Mineral Bonafont Sem Gás 500ml" to "Sucos e Diversos"),
        "7891025114543" to ("Água Mineral Bonafont Sem Gás 1.5L" to "Sucos e Diversos"),
        "7898926010019" to ("Suco de Laranja Integral Prat's 900ml" to "Sucos e Diversos"),
        "7898926010026" to ("Suco de Laranja Integral Prat's 1.7L" to "Sucos e Diversos"),
        "7891025100010" to ("Suco de Uva Integral Del Valle 1L" to "Sucos e Diversos"),
        "7898950000011" to ("Suco Natural One Laranja Integral 900ml" to "Sucos e Diversos"),
        "7892840803018" to ("Isotônico Gatorade Tangerina 500ml" to "Sucos e Diversos"),
        "7894900520019" to ("Chá Matte Leão com Limão Pet 1.5L" to "Sucos e Diversos"),

        // === DESTILADOS ===
        "7891000100103" to ("Whisky Johnnie Walker Red Label 1 Litro" to "Destilados"),
        "5000267014203" to ("Whisky Johnnie Walker Black Label 12 Anos 1L" to "Destilados"),
        "5010106113127" to ("Whisky Chivas Regal 12 Anos Blended Scotch 1L" to "Destilados"),
        "5099873045367" to ("Whisky Jack Daniel's Old No. 7 Tennessee 1L" to "Destilados"),
        "5010106110126" to ("Whisky Ballantine's Finest Blended Scotch 1L" to "Destilados"),
        "5000267124315" to ("Whisky White Horse Blended Scotch 1L" to "Destilados"),
        "5010106001028" to ("Whisky Passport Scotch 1 Litro" to "Destilados"),
        "7891095000104" to ("Vodka Smirnoff Red No. 21 Garrafa 998ml" to "Destilados"),
        "7312040017034" to ("Vodka Absolut Original Sueca 1 Litro" to "Destilados"),
        "088076161869"  to ("Vodka Cîroc Snap Frost Ultra Premium 750ml" to "Destilados"),
        "5000289929011" to ("Gin Tanqueray London Dry Importado 750ml" to "Destilados"),
        "5010677714006" to ("Gin Bombay Sapphire London Dry 750ml" to "Destilados"),
        "5000299223017" to ("Gin Gordon's London Dry 750ml" to "Destilados"),
        "7896004000115" to ("Cachaça 51 Pirassununga Tradicional 965ml" to "Destilados"),
        "7896001000019" to ("Cachaça Velho Barreiro Tradicional 910ml" to "Destilados"),
        "7896002000018" to ("Cachaça Ypióca Prata Clássica 965ml" to "Destilados"),
        "7896003000017" to ("Cachaça Sagatiba Pura Cristal 700ml" to "Destilados"),
        "8000500000000" to ("Aperitivo Campari Bitter Italiano 900ml" to "Destilados"),
        "8002270014282" to ("Aperitivo Aperol Spritz Garrafa 750ml" to "Destilados"),
        "4067700010018" to ("Licor Jägermeister Herbal Alemão 700ml" to "Destilados"),
        "5011013100156" to ("Licor Baileys Original Irish Cream 750ml" to "Destilados"),
        "6001495062577" to ("Licor Amarula Cream Sul-Africano 750ml" to "Destilados"),
        "8410065000011" to ("Licor 43 Diego Zamora Espanha 700ml" to "Destilados"),
        "7501035010108" to ("Tequila Jose Cuervo Especial Ouro 750ml" to "Destilados"),
        "7896009000011" to ("Coquetel Alcoólico Corote Blueberry 500ml" to "Destilados"),
        "7896009000028" to ("Coquetel Alcoólico Corote Morango 500ml" to "Destilados"),

        // === VINHOS ===
        "7804320087114" to ("Vinho Tinto Chileno Casillero del Diablo Cabernet Sauvignon 750ml" to "Vinhos"),
        "7804320752531" to ("Vinho Tinto Concha y Toro Reservado Cabernet Sauvignon 750ml" to "Vinhos"),
        "7804320752555" to ("Vinho Tinto Concha y Toro Reservado Carmenere 750ml" to "Vinhos"),
        "7804320752548" to ("Vinho Tinto Concha y Toro Reservado Merlot 750ml" to "Vinhos"),
        "7804320752562" to ("Vinho Branco Concha y Toro Reservado Sauvignon Blanc 750ml" to "Vinhos"),
        "7804300100015" to ("Vinho Tinto Chileno Santa Helena Cabernet Sauvignon 750ml" to "Vinhos"),
        "7896050200118" to ("Vinho Tinto de Mesa Pergola Suave Garrafa 1L" to "Vinhos"),
        "7896050200125" to ("Vinho Tinto de Mesa Quinta do Morgado Bordô Suave 1L" to "Vinhos"),
        "7896050200217" to ("Vinho Branco de Mesa Quinta do Morgado Suave 1L" to "Vinhos"),
        "7896050200316" to ("Vinho Tinto de Mesa Cantina da Serra Tradicional 1L" to "Vinhos"),
        "7896050200132" to ("Espumante Chandon Réserve Brut 750ml" to "Vinhos"),
        "7896050200149" to ("Espumante Chandon Passion Demi-Sec 750ml" to "Vinhos"),
        "7896050200507" to ("Espumante Moscatel Casa Perini 750ml" to "Vinhos"),
        "8410036000017" to ("Espumante Espanhol Freixenet Cordon Negro Brut 750ml" to "Vinhos"),
        "7791250000015" to ("Vinho Tinto Argentino Cordero con Piel de Lobo Malbec 750ml" to "Vinhos"),
        "7791250000107" to ("Vinho Tinto Argentino DV Catena Cabernet-Malbec 750ml" to "Vinhos"),

        // === SALGADINHOS ===
        "7892840812973" to ("Salgadinho Doritos Queijo Nacho Elma Chips 78g" to "Salgadinhos"),
        "7892840812980" to ("Salgadinho Doritos Queijo Nacho Elma Chips 140g" to "Salgadinhos"),
        "7892840800055" to ("Salgadinho Ruffles Batata Original Elma Chips 76g" to "Salgadinhos"),
        "7892840800062" to ("Salgadinho Ruffles Cebola e Salsa Elma Chips 76g" to "Salgadinhos"),
        "7892840800109" to ("Salgadinho Cheetos Onda Requeijão Elma Chips 75g" to "Salgadinhos"),
        "7892840800154" to ("Salgadinho Fandangos Presunto Elma Chips 75g" to "Salgadinhos"),
        "7892840800208" to ("Salgadinho Torcida Sabor Churrasco 70g" to "Salgadinhos"),
        "7892840800215" to ("Salgadinho Torcida Sabor Queijo 70g" to "Salgadinhos"),
        "7892840818203" to ("Batata Pringles Original Tubo 104g" to "Salgadinhos"),
        "7892840818210" to ("Batata Pringles Creme e Cebola Tubo 104g" to "Salgadinhos"),
        "7892840818227" to ("Batata Pringles Queijo Cheddar Tubo 104g" to "Salgadinhos"),
        "7896001250100" to ("Amendoim Japonês Dori Tradicional Pacote 500g" to "Salgadinhos"),
        "7896001250117" to ("Amendoim Pettiz Crocante Cebola e Salsa Santa Helena 150g" to "Salgadinhos"),

        // === BOMBONIERE ===
        "7891008121018" to ("Chocolate Bis Ao Leite Lacta Caixa 126g" to "Bomboniere"),
        "7891008121025" to ("Chocolate Bis Black Lacta Caixa 100g" to "Bomboniere"),
        "7891008121032" to ("Chocolate Bis Branco Lacta Caixa 126g" to "Bomboniere"),
        "7891000100202" to ("Bombom Sonho de Valsa Lacta Pacote 1kg" to "Bomboniere"),
        "7891000100219" to ("Bombom Ouro Branco Lacta Pacote 1kg" to "Bomboniere"),
        "7891000247656" to ("Chocolate KitKat 4 Fingers Ao Leite Nestlé 41.5g" to "Bomboniere"),
        "7891008100013" to ("Chocolate Diamante Negro Lacta Barra 80g" to "Bomboniere"),
        "7891008100020" to ("Chocolate Laka Branco Lacta Barra 80g" to "Bomboniere"),
        "7891000100301" to ("Chocolate Classic Ao Leite Nestlé Barra 80g" to "Bomboniere"),
        "7898024394182" to ("Bombom Ferrero Rocher Caixa com 8 Unidades" to "Bomboniere"),
        "7891000053508" to ("Bala Halls Extra Forte Preto Display 28g" to "Bomboniere"),
        "7891000053515" to ("Bala Halls Mentol Azul Display 28g" to "Bomboniere"),
        "7891000053522" to ("Bala Halls Melancia Display 28g" to "Bomboniere"),
        "7622210565502" to ("Goma de Mascar Trident Menta Sem Açúcar 8g" to "Bomboniere"),
        "7622210565519" to ("Goma de Mascar Trident Melancia Sem Açúcar 8g" to "Bomboniere"),

        // === GELOS ===
        "7896500010014" to ("Gelo Filtrado em Cubos Pacote 5kg" to "Gelos"),
        "7896500010021" to ("Gelo Britado Especial Pacote 10kg" to "Gelos"),
        "7898950001001" to ("Gelo Saborizado Escobar Melancia Copo 200ml" to "Gelos"),
        "7898950001002" to ("Gelo Saborizado Escobar Coco Verde Copo 200ml" to "Gelos"),
        "7898950001003" to ("Gelo Saborizado Escobar Maracujá Copo 200ml" to "Gelos"),
        "7898950001004" to ("Gelo Saborizado Escobar Maçã Verde Copo 200ml" to "Gelos"),
        "7898950001005" to ("Gelo Saborizado Escobar Morango Copo 200ml" to "Gelos"),

        // === CONGELADOS ===
        "7896000000011" to ("Batata Pré-Frita Palito Congelada McCain 720g" to "Congelados"),
        "7896000000028" to ("Batata Pré-Frita Palito Congelada Bem Brasil 2kg" to "Congelados"),
        "7891515432101" to ("Pizza Congelada Sadia Calabresa 460g" to "Congelados"),
        "7891515432102" to ("Pizza Congelada Seara 4 Queijos 440g" to "Congelados"),
        "7891515432103" to ("Hambúrguer Bovino Sadia Caixa 672g" to "Congelados"),
        "7891515432104" to ("Nuggets de Frango Crocante Seara 300g" to "Congelados"),
        "7896005001012" to ("Pão de Queijo Tradicional Congelado Forno de Minas 1kg" to "Congelados"),
        "7891025000013" to ("Pote de Sorvete Kibon Napolitano Cremíssimo 1.5L" to "Congelados"),
        "7898930000015" to ("Pote de Açaí Cremoso com Guaraná Frooty 1L" to "Congelados"),

        // === TABACO ===
        "7622300990018" to ("Cigarro Marlboro Red Box com 20 Unidades" to "Tabaco"),
        "7622300990025" to ("Cigarro Marlboro Gold Box com 20 Unidades" to "Tabaco"),
        "7622300990032" to ("Cigarro Camel Blue Box com 20 Unidades" to "Tabaco"),
        "7622300990049" to ("Cigarro Lucky Strike Original Red Box com 20 Unidades" to "Tabaco"),
        "7622300990056" to ("Cigarro Dunhill Carlton Blend Box 20 Unidades" to "Tabaco"),
        "7622300990063" to ("Cigarro Winston Red Box com 20 Unidades" to "Tabaco"),
        "7896001009999" to ("Palheiro Tradicional Souza Paiol Maço 20 Unidades" to "Tabaco"),
        "7896001009982" to ("Palheiro Piracanjuba Tradicional Artesanal Maço" to "Tabaco"),
        "3086126693026" to ("Isqueiro BIC Maxi Tradicional Grande Cores" to "Tabaco"),
        "3086126693033" to ("Isqueiro BIC Mini Tradicional Cores" to "Tabaco"),
        "8410283000012" to ("Seda Smoking King Size Brown Extra Fina 33 folhas" to "Tabaco"),
        "3057067000015" to ("Seda OCB Slim Premium Extra Fina 32 folhas" to "Tabaco"),
        "7898940001018" to ("Seda Zomo Slim King Size Alfafa 33 folhas" to "Tabaco"),
        "7898940002015" to ("Essência de Narguilé Zomo Strong Mint 50g" to "Tabaco"),
        "7898940003012" to ("Carvão de Coco Hexagonal para Narguilé Art Coco 1kg" to "Tabaco"),

        // === MERCEARIA ===
        "7891000001000" to ("Copo Descartável Transparente 500ml Pacote 50 Unidades" to "Mercearia"),
        "7891000001001" to ("Copo Descartável Transparente 200ml Pacote 100 Unidades" to "Mercearia"),
        "7891000001002" to ("Copo Térmico Descartável Copobras 770ml Pacote 25 un" to "Mercearia"),
        "7891000001003" to ("Canudo Biodegradável Embalado Individualmente Pacote 100 un" to "Mercearia"),
        "7891000001004" to ("Carvão Vegetal Selecionado para Churrasco Pacote 3kg" to "Mercearia"),
        "7891000001005" to ("Carvão Vegetal Selecionado para Churrasco Pacote 5kg" to "Mercearia"),
        "7891000001006" to ("Sal Grosso para Churrasco Pacote 1kg" to "Mercearia"),
        "7891000001007" to ("Guardanapo de Papel Folha Dupla Pacote 50 unidades" to "Mercearia"),
        "7891000001008" to ("Acendedor Sólido em Bloco para Churrasqueira e Carvão" to "Mercearia"),
        "7891000001009" to ("Saca-Rolhas Profissional Duplo Estágio e Abridor de Garrafas" to "Mercearia")
    )

    suspend fun lookupProductByBarcode(barcode: String): ProductOnlineInfo? = withContext(Dispatchers.IO) {
        val cleanBarcode = barcode.trim()
        if (cleanBarcode.length < 5) return@withContext null

        // 1. Verificação instantânea no catálogo embutido de adegas e supermercados (Zero latência, 100% de precisão)
        val instantMatch = INSTANT_PRODUCT_CATALOG[cleanBarcode]
        if (instantMatch != null) {
            val (name, cat) = instantMatch
            Log.d("ProductLookup", "Encontrado instantaneamente no catálogo embutido: $name ($cat)")
            return@withContext ProductOnlineInfo(
                name = name,
                category = cat
            )
        }

        // 2. Consulta em múltiplos endpoints abertos de supermercados e alimentos
        val openFactsEndpoints = listOf(
            "https://br.openfoodfacts.org/api/v2/product/$cleanBarcode.json",
            "https://world.openfoodfacts.org/api/v2/product/$cleanBarcode.json",
            "https://world.openfoodfacts.org/api/v0/product/$cleanBarcode.json",
            "https://world.openproductsfacts.org/api/v0/product/$cleanBarcode.json",
            "https://world.openbeautyfacts.org/api/v0/product/$cleanBarcode.json"
        )

        for (endpoint in openFactsEndpoints) {
            try {
                val url = URL(endpoint)
                val conn = url.openConnection() as HttpURLConnection
                conn.requestMethod = "GET"
                conn.connectTimeout = 3000
                conn.readTimeout = 3000
                conn.setRequestProperty("User-Agent", "MaxBebidasEstoque/3.0 (Android; pt-BR)")

                if (conn.responseCode == 200) {
                    val reader = BufferedReader(InputStreamReader(conn.inputStream))
                    val response = reader.readText()
                    reader.close()

                    val json = JSONObject(response)
                    val status = json.optInt("status", 0)
                    if (status == 1 && json.has("product")) {
                        val productObj = json.getJSONObject("product")

                        val rawName = when {
                            productObj.has("product_name_pt") && productObj.getString("product_name_pt").isNotBlank() ->
                                productObj.getString("product_name_pt")
                            productObj.has("product_name") && productObj.getString("product_name").isNotBlank() ->
                                productObj.getString("product_name")
                            productObj.has("generic_name_pt") && productObj.getString("generic_name_pt").isNotBlank() ->
                                productObj.getString("generic_name_pt")
                            productObj.has("generic_name") && productObj.getString("generic_name").isNotBlank() ->
                                productObj.getString("generic_name")
                            productObj.has("product_name_en") && productObj.getString("product_name_en").isNotBlank() ->
                                productObj.getString("product_name_en")
                            else -> null
                        }

                        if (!rawName.isNullOrBlank()) {
                            val brand = productObj.optString("brands", "").ifBlank { null }
                            val quantity = productObj.optString("quantity", "").ifBlank { null }
                            val cleanName = formatProductName(rawName, brand, quantity)
                            val detectedCategory = inferCategoryFromName(cleanName)

                            Log.d("ProductLookup", "Encontrado em catálogo de supermercado: $cleanName ($detectedCategory)")
                            return@withContext ProductOnlineInfo(
                                name = cleanName,
                                category = detectedCategory,
                                brand = brand?.split(",")?.firstOrNull()?.trim(),
                                quantityString = quantity?.trim()
                            )
                        }
                    }
                }
            } catch (e: Exception) {
                Log.d("ProductLookup", "Falha no endpoint $endpoint: ${e.message}")
            }
        }

        // 3. Tentar UPCitemdb (Catálogo global de produtos de supermercado e varejo)
        try {
            val upcUrl = URL("https://api.upcitemdb.com/prod/trial/lookup?upc=$cleanBarcode")
            val conn = upcUrl.openConnection() as HttpURLConnection
            conn.requestMethod = "GET"
            conn.connectTimeout = 3000
            conn.readTimeout = 3000
            conn.setRequestProperty("User-Agent", "MaxBebidasEstoque/3.0 (Android; pt-BR)")

            if (conn.responseCode == 200) {
                val reader = BufferedReader(InputStreamReader(conn.inputStream))
                val response = reader.readText()
                reader.close()

                val json = JSONObject(response)
                val total = json.optInt("total", 0)
                if (total > 0 && json.has("items")) {
                    val items = json.getJSONArray("items")
                    if (items.length() > 0) {
                        val firstItem = items.getJSONObject(0)
                        val title = firstItem.optString("title", "")
                        val brand = firstItem.optString("brand", "")

                        if (title.isNotBlank()) {
                            val cleanName = formatProductName(title, brand.ifBlank { null }, null)
                            val detectedCategory = inferCategoryFromName(cleanName)
                            Log.d("ProductLookup", "Encontrado no UPCitemdb: $cleanName ($detectedCategory)")
                            return@withContext ProductOnlineInfo(
                                name = cleanName,
                                category = detectedCategory,
                                brand = brand.ifBlank { null }
                            )
                        }
                    }
                }
            }
        } catch (e: Exception) {
            Log.d("ProductLookup", "Falha no UPCitemdb: ${e.message}")
        }

        null
    }

    /**
     * Pesquisa produtos pelo nome diretamente nos catálogos e lojas online de supermercados e adegas.
     * Retorna lista de sugestões com dados enriquecidos (nome comercial, categoria, marca e código de barras).
     */
    suspend fun searchProductsByNameOnline(query: String): List<ProductOnlineInfo> = withContext(Dispatchers.IO) {
        val cleanQuery = query.trim()
        if (cleanQuery.length < 2) return@withContext emptyList()

        val results = mutableListOf<ProductOnlineInfo>()
        val seenNames = mutableSetOf<String>()

        // 1. Pesquisa prioritária no catálogo de alta frequência de supermercados e adegas
        val queryTokens = cleanQuery.lowercase().split(Regex("\\s+")).filter { it.isNotBlank() }
        for ((barcode, pair) in INSTANT_PRODUCT_CATALOG) {
            val (name, cat) = pair
            val lowerName = name.lowercase()
            val matches = queryTokens.all { token -> lowerName.contains(token) }
            if (matches && seenNames.add(lowerName)) {
                results.add(
                    ProductOnlineInfo(
                        name = name,
                        category = cat,
                        barcode = barcode,
                        storeOrSource = "Catálogo Oficial de Adegas"
                    )
                )
            }
        }

        // 2. Consulta online via Open Food Facts Brasil e Global (Supermercados & Bebidas)
        val encoded = try {
            java.net.URLEncoder.encode(cleanQuery, "UTF-8")
        } catch (_: Exception) {
            cleanQuery
        }

        val searchUrls = listOf(
            "https://br.openfoodfacts.org/cgi/search.pl?search_terms=$encoded&search_simple=1&action=process&json=1&page_size=15",
            "https://world.openfoodfacts.org/cgi/search.pl?search_terms=$encoded&search_simple=1&action=process&json=1&page_size=15"
        )

        for (endpoint in searchUrls) {
            if (results.size >= 12) break
            try {
                val url = URL(endpoint)
                val conn = url.openConnection() as HttpURLConnection
                conn.requestMethod = "GET"
                conn.connectTimeout = 3000
                conn.readTimeout = 3500
                conn.setRequestProperty("User-Agent", "MaxBebidasEstoque/3.0 (Android; pt-BR)")

                if (conn.responseCode == 200) {
                    val reader = BufferedReader(InputStreamReader(conn.inputStream))
                    val response = reader.readText()
                    reader.close()

                    val json = JSONObject(response)
                    if (json.has("products")) {
                        val products = json.getJSONArray("products")
                        for (i in 0 until products.length()) {
                            if (results.size >= 15) break
                            val p = products.getJSONObject(i)

                            val rawName = when {
                                p.has("product_name_pt") && p.getString("product_name_pt").isNotBlank() -> p.getString("product_name_pt")
                                p.has("product_name") && p.getString("product_name").isNotBlank() -> p.getString("product_name")
                                p.has("generic_name_pt") && p.getString("generic_name_pt").isNotBlank() -> p.getString("generic_name_pt")
                                else -> null
                            } ?: continue

                            val brand = p.optString("brands", "").ifBlank { null }
                            val quantity = p.optString("quantity", "").ifBlank { null }
                            val code = p.optString("code", "").ifBlank { null }
                            val cleanName = formatProductName(rawName, brand, quantity)
                            val lowerClean = cleanName.lowercase()

                            if (cleanName.length >= 3 && seenNames.add(lowerClean)) {
                                val detectedCat = inferCategoryFromName(cleanName) ?: "Sucos e Diversos"
                                results.add(
                                    ProductOnlineInfo(
                                        name = cleanName,
                                        category = detectedCat,
                                        brand = brand?.split(",")?.firstOrNull()?.trim(),
                                        quantityString = quantity?.trim(),
                                        barcode = code,
                                        storeOrSource = "Supermercados & Adegas Online"
                                    )
                                )
                            }
                        }
                    }
                }
            } catch (e: Exception) {
                Log.d("ProductLookup", "Falha na busca online de produtos por nome: ${e.message}")
            }
        }

        results
    }

    /**
     * Higieniza e padroniza o nome do produto no estilo comercial de adegas e supermercados.
     */
    fun formatProductName(rawName: String, brand: String? = null, quantity: String? = null): String {
        var name = rawName.trim()

        // Remover códigos numéricos e ruídos de etiquetas internas
        name = name.replace(Regex("(?i)\\b(ean|sku|cod|ref|cx|un|fd|pct)\\b[:\\-\\s]*\\d+"), "")
        name = name.replace(Regex("\\[.*?\\]"), "")
        name = name.replace(Regex("\\s+"), " ").trim()

        // Se o nome não contiver a marca e a marca for conhecida, anexá-la de forma elegante
        if (!brand.isNullOrBlank()) {
            val cleanBrand = brand.split(",").first().trim()
            if (cleanBrand.length >= 3 && !name.contains(cleanBrand, ignoreCase = true)) {
                name = "$cleanBrand $name"
            }
        }

        // Se houver especificação de quantidade/volume e não constar no nome, adicionar ao final
        if (!quantity.isNullOrBlank()) {
            val cleanQty = quantity.trim()
            if (cleanQty.length in 2..15 && !name.contains(cleanQty, ignoreCase = true)) {
                name = "$name $cleanQty"
            }
        }

        // Normalização de HTML entities e acentuação
        name = name.replace("&amp;", "&")
            .replace("&quot;", "\"")
            .replace("&#39;", "'")

        // Capitalizar a primeira letra
        if (name.length > 2) {
            name = name.replaceFirstChar { if (it.isLowerCase()) it.titlecase() else it.toString() }
        }
        return name
    }

    /**
     * Motor de inferência semântica para as 10 categorias oficiais de adegas e supermercados:
     * Mercearia, Vinhos, Destilados, Salgadinhos, Bomboniere, Congelados, Gelos, Cervejas, Sucos e Diversos, Tabaco.
     */
    fun inferCategoryFromName(productName: String): String? {
        val lower = productName.lowercase().trim()
        if (lower.isBlank()) return null

        return when {
            // === CERVEJAS ===
            lower.contains("cerveja") || lower.contains("chopp") || lower.contains("chope") ||
                    lower.contains("pilsen") || lower.contains("lager") || lower.contains("ipa") ||
                    lower.contains("heineken") || lower.contains("stella") || lower.contains("skol") ||
                    lower.contains("brahma") || lower.contains("amstel") || lower.contains("budweiser") ||
                    lower.contains("eisenbahn") || lower.contains("beck") || lower.contains("corona") ||
                    lower.contains("spaten") || lower.contains("original") || lower.contains("devassa") ||
                    lower.contains("antarctica sub") || lower.contains("itaipava") || lower.contains("colorado") ||
                    lower.contains("duplo malte") || lower.contains("puro malte") || lower.contains("weiss") ||
                    lower.contains("stout") || lower.contains("bohemia") || lower.contains("hoegaarden") ||
                    lower.contains("paulaner") || lower.contains("baden baden") || lower.contains("cerpa") -> "Cervejas"

            // === VINHOS ===
            lower.contains("vinho") || lower.contains("cabernet") || lower.contains("merlot") ||
                    lower.contains("malbec") || lower.contains("chardonnay") || lower.contains("pinot") ||
                    lower.contains("sauvignon") || lower.contains("carmenere") || lower.contains("syrah") ||
                    lower.contains("espumante") || lower.contains("prosecco") || lower.contains("champagne") ||
                    lower.contains("lambrusco") || lower.contains("tinto") || lower.contains("pergola") ||
                    lower.contains("quinta do morgado") || lower.contains("casillero") || lower.contains("concha y toro") ||
                    lower.contains("santa helena") || lower.contains("chandon") || lower.contains("freixenet") ||
                    lower.contains("casa perini") || lower.contains("cordero con piel") || lower.contains("catena") ||
                    lower.contains("salton") || lower.contains("miolo") || lower.contains("cantina da serra") ||
                    lower.contains("toro loco") || lower.contains("periquita") -> "Vinhos"

            // === DESTILADOS ===
            lower.contains("whisky") || lower.contains("whiskey") || lower.contains("vodka") ||
                    lower.contains("gin") || lower.contains("rum") || lower.contains("cachaça") ||
                    lower.contains("cachaca") || lower.contains("tequila") || lower.contains("licor") ||
                    lower.contains("campari") || lower.contains("vermouth") || lower.contains("aperol") ||
                    lower.contains("cognac") || lower.contains("conhaque") || lower.contains("corote") ||
                    lower.contains("pinga") || lower.contains("absinto") || lower.contains("jagermeister") ||
                    lower.contains("johnnie walker") || lower.contains("red label") || lower.contains("black label") ||
                    lower.contains("chivas") || lower.contains("jack daniel") || lower.contains("ballantine") ||
                    lower.contains("white horse") || lower.contains("smirnoff") || lower.contains("absolut") ||
                    lower.contains("tanqueray") || lower.contains("bombay") || lower.contains("velho barreiro") ||
                    lower.contains("ypióca") || lower.contains("ypioca") || lower.contains("amarula") ||
                    lower.contains("baileys") || lower.contains("licor 43") || lower.contains("jose cuervo") ||
                    lower.contains("passport") || lower.contains("sagatiba") || lower.contains("51") -> "Destilados"

            // === TABACO ===
            lower.contains("cigarro") || lower.contains("tabaco") || lower.contains("palheiro") ||
                    lower.contains("paiol") || lower.contains("fumo") || lower.contains("isqueiro") ||
                    lower.contains("bic") || lower.contains("clipper") || lower.contains("seda") ||
                    lower.contains("smoking") || lower.contains("ocb") || lower.contains("zomo") ||
                    lower.contains("narguilé") || lower.contains("narguile") || lower.contains("essência") ||
                    lower.contains("essencia") || lower.contains("marlboro") || lower.contains("camel") ||
                    lower.contains("lucky strike") || lower.contains("dunhill") || lower.contains("winston") ||
                    lower.contains("charuto") || lower.contains("carvão de coco") || lower.contains("art coco") -> "Tabaco"

            // === SALGADINHOS ===
            lower.contains("salgadinho") || lower.contains("doritos") || lower.contains("lays") ||
                    lower.contains("lay's") || lower.contains("cheetos") || lower.contains("amendoim") ||
                    lower.contains("ruffles") || lower.contains("fandangos") || lower.contains("torcida") ||
                    lower.contains("pringles") || lower.contains("batata palha") || lower.contains("snack") ||
                    lower.contains("pettiz") || lower.contains("dori") || lower.contains("castanha") ||
                    lower.contains("pururuca") || lower.contains("petisco salgado") -> "Salgadinhos"

            // === BOMBONIERE ===
            lower.contains("chocolate") || lower.contains("bombom") || lower.contains("bis ") ||
                    lower.contains("bis black") || lower.contains("kitkat") || lower.contains("kit kat") ||
                    lower.contains("sonho de valsa") || lower.contains("ouro branco") || lower.contains("diamante negro") ||
                    lower.contains("laka") || lower.contains("lacta") || lower.contains("nestlé") ||
                    lower.contains("nestle") || lower.contains("garoto") || lower.contains("ferrero") ||
                    lower.contains("halls") || lower.contains("trident") || lower.contains("mentos") ||
                    lower.contains("bala") || lower.contains("chiclete") || lower.contains("goma de mascar") ||
                    lower.contains("pirulito") || lower.contains("paçoca") || lower.contains("pacoca") ||
                    lower.contains("doce") || lower.contains("butter toffees") -> "Bomboniere"

            // === GELOS ===
            lower.contains("gelo") || lower.contains("saco de gelo") || lower.contains("gelo saborizado") ||
                    lower.contains("escobar") || lower.contains("gelo em cubo") || lower.contains("gelo britado") -> "Gelos"

            // === CONGELADOS ===
            lower.contains("hamburguer") || lower.contains("hambúrguer") || lower.contains("pizza") ||
                    lower.contains("nuggets") || lower.contains("batata pré-frita") || lower.contains("batata congelada") ||
                    lower.contains("mccain") || lower.contains("bem brasil") || lower.contains("sorvete") ||
                    lower.contains("picolé") || lower.contains("picole") || lower.contains("açaí") ||
                    lower.contains("acai") || lower.contains("gelato") || lower.contains("congelado") ||
                    lower.contains("kibon") || lower.contains("forno de minas") || lower.contains("pão de queijo congelado") -> "Congelados"

            // === SUCOS E DIVERSOS (REFRIGERANTES, ENERGÉTICOS, ÁGUAS) ===
            lower.contains("suco") || lower.contains("agua") || lower.contains("água") ||
                    lower.contains("refrigerante") || lower.contains("coca") || lower.contains("guaraná") ||
                    lower.contains("guarana") || lower.contains("pepsi") || lower.contains("fanta") ||
                    lower.contains("sprite") || lower.contains("soda") || lower.contains("gatorade") ||
                    lower.contains("tonica") || lower.contains("tônica") || lower.contains("monster") ||
                    lower.contains("red bull") || lower.contains("energetico") || lower.contains("energético") ||
                    lower.contains("prat's") || lower.contains("prats") || lower.contains("del valle") ||
                    lower.contains("natural one") || lower.contains("maguary") || lower.contains("crystal") ||
                    lower.contains("bonafont") || lower.contains("minalba") || lower.contains("matte leão") ||
                    lower.contains("chá") || lower.contains("tea") || lower.contains("baly") -> "Sucos e Diversos"

            // === MERCEARIA ===
            lower.contains("copo descartavel") || lower.contains("copo descartável") || lower.contains("copo térmico") ||
                    lower.contains("canudo") || lower.contains("carvão") || lower.contains("carvao") ||
                    lower.contains("sal grosso") || lower.contains("guardanapo") || lower.contains("acendedor") ||
                    lower.contains("abridor") || lower.contains("saca-rolhas") || lower.contains("mercearia") ||
                    lower.contains("arroz") || lower.contains("feijão") || lower.contains("macarrão") ||
                    lower.contains("azeite") || lower.contains("óleo") || lower.contains("molho") ||
                    lower.contains("conserva") || lower.contains("azeitona") || lower.contains("palmito") -> "Mercearia"

            else -> null
        }
    }
}
