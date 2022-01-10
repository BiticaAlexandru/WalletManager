package repository;

import controller.WalletDTO;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Optional;

public class DatabaseWalletRepository implements WalletRepository {

    private final Connection connection;

    public DatabaseWalletRepository(Connection connection) {
        this.connection = connection;
    }

    @Override
    public Optional<WalletDTO> getWallet(String id) {
        try (PreparedStatement getRequest = connection.prepareStatement("select * from lnd_wallet.wallet where id like ?;")){
            getRequest.setString(1, id);
            ResultSet resultSet = getRequest.executeQuery();
            WalletDTO walletDTO = parseResultSet(resultSet);
            return Optional.ofNullable(walletDTO);
        } catch (SQLException throwables) {
            throw new DatabaseException(throwables);
        }
    }

    private WalletDTO parseResultSet(ResultSet resultSet) throws SQLException {
        if(resultSet.next()) {
            String trx = resultSet.getString("trx");
            int version = resultSet.getInt("version");
            double balance = resultSet.getDouble("balance");
            return new WalletDTO(trx, version, balance);
        }
        return null;
    }

    @Override
    public void updateWallet(String id, WalletDTO walletDTO) {
        try (PreparedStatement createRequest = connection.prepareStatement("update lnd_wallet.wallet set trx=?, balance=?, version=? where id like ? and version = ?;")) {
            createRequest.setString(1, walletDTO.getTransactionId());
            createRequest.setDouble(2, walletDTO.getCoins());
            createRequest.setInt(3, (int) walletDTO.getVersion());
            createRequest.setString(4, id);
            createRequest.setInt(5,  (int) walletDTO.getVersion() - 1);
            createRequest.execute();
        } catch (SQLException throwables) {
            throw new DatabaseException(throwables);
        }
    }

    @Override
    public WalletDTO createNewWallet(String id) {
        try (PreparedStatement createRequest = connection.prepareStatement("insert into lnd_wallet.wallet values(?, 0,'', 0 );")) {
            createRequest.setString(1, id);
            createRequest.execute();
        } catch (SQLException throwables) {
            throw new DatabaseException(throwables);
        }
        return new WalletDTO("", 0, 0);
    }

    @Override
    public Optional<String> getLastTransactionByWalletId(String id) {
        try (PreparedStatement getRequest = connection.prepareStatement("select trx from lnd_wallet.wallet where id like ?;")){
            getRequest.setString(1, id);
            ResultSet resultSet = getRequest.executeQuery();
            String transactionId = "";
            if(resultSet.next()) {
                transactionId = resultSet.getString("trx");
            }
            return Optional.ofNullable(transactionId);
        } catch (SQLException throwables) {
            throw new DatabaseException(throwables);
        }
    }
}
