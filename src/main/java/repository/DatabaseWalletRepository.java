package repository;

import controller.WalletDTO;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Optional;

import static repository.RepositoryConstants.*;

public class DatabaseWalletRepository implements WalletRepository {

    private static final String GET_WALLET_SQL_TEMPLATE = "select * from lnd_wallet.wallet where id like ?;";
    private static final String UPDATE_WALLET_SQL_TEMPLATE = "update lnd_wallet.wallet set trx=?, balance=?, version=? where id like ? and version = ?;";
    private static final String INSERT_WALLET_SQL_TEMPLATE = "insert into lnd_wallet.wallet values(?, 0,'', 0 );";
    private static final String GET_TRANSACTION_SQL_TEMPLATE = "select trx from lnd_wallet.wallet where id like ?;";

    private static final String TRANSACTION_COLUMN = "trx";
    private static final String VERSION_COLUMN = "version";
    private static final String BALANCE_COLUMN = "balance";

    private final Connection connection;

    public DatabaseWalletRepository(Connection connection) {
        this.connection = connection;
    }

    @Override
    public Optional<WalletDTO> getWallet(String id) {
        try (PreparedStatement getRequest = connection.prepareStatement(GET_WALLET_SQL_TEMPLATE)) {
            getRequest.setString(1, id);
            ResultSet resultSet = getRequest.executeQuery();
            WalletDTO walletDTO = parseResultSet(resultSet);
            return Optional.ofNullable(walletDTO);
        } catch (SQLException throwables) {
            throw new DatabaseException(throwables);
        }
    }

    private WalletDTO parseResultSet(ResultSet resultSet) throws SQLException {
        if (resultSet.next()) {
            String trx = resultSet.getString(TRANSACTION_COLUMN);
            int version = resultSet.getInt(VERSION_COLUMN);
            double balance = resultSet.getDouble(BALANCE_COLUMN);
            return new WalletDTO(trx, version, balance);
        }
        return null;
    }

    @Override
    public void updateWallet(String id, WalletDTO walletDTO) {
        try (PreparedStatement createRequest = connection.prepareStatement(UPDATE_WALLET_SQL_TEMPLATE)) {
            createRequest.setString(1, walletDTO.getTransactionId());
            createRequest.setDouble(2, walletDTO.getCoins());
            createRequest.setInt(3, (int) walletDTO.getVersion());
            createRequest.setString(4, id);
            int currentVersion = (int) walletDTO.getVersion() - 1;
            createRequest.setInt(5, currentVersion);
            createRequest.execute();
        } catch (SQLException exception) {
            throw new DatabaseException(exception);
        }
    }

    @Override
    public WalletDTO createNewWallet(String id) {
        try (PreparedStatement createRequest = connection.prepareStatement(INSERT_WALLET_SQL_TEMPLATE)) {
            createRequest.setString(1, id);
            createRequest.execute();
        } catch (SQLException exception) {
            throw new DatabaseException(exception);
        }
        return new WalletDTO(EMPTY_TRANSACTION_ID, FIRST_VERSION, NO_COINS);
    }

    @Override
    public Optional<String> getLastTransactionByWalletId(String id) {
        try (PreparedStatement getRequest = connection.prepareStatement(GET_TRANSACTION_SQL_TEMPLATE)) {
            getRequest.setString(1, id);
            ResultSet resultSet = getRequest.executeQuery();
            String transactionId = EMPTY_TRANSACTION_ID;
            if (resultSet.next()) {
                transactionId = resultSet.getString(TRANSACTION_COLUMN);
            }
            return Optional.ofNullable(transactionId);
        } catch (SQLException exception) {
            throw new DatabaseException(exception);//TODO HANDLE
        }
    }
}
